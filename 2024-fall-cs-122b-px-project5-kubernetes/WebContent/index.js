$(document).ready(function () {
    console.log("index.js loaded successfully");

    let currentPage = 1;
    let N = 10;
    const cache = {}; // Cache object for autocomplete

    // Fetch top 20 movies for the background
    function fetchTopMovies() {
        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies",
            data: { page: 1, N: 20, primarySortBy: "rating", primarySortOrder: "DESC" },
            success: (resultData) => {
                const moviePostersElement = $("#movie-posters");
                moviePostersElement.empty();
                resultData.forEach((movie) => {
                    const posterUrl = movie.posterUrl || "res/images/default-poster.jpg"; // Default poster
                    const posterHTML = `
                        <div style="margin: 10px; text-align: center;">
                            <img src="${posterUrl}" alt="${movie.title}" style="width: 150px; height: 200px;">
                            <div style="color: white;">${movie.title}</div>
                        </div>
                    `;
                    moviePostersElement.append(posterHTML);
                });
            },
            error: (jqXHR, textStatus, errorThrown) => {
                console.error("Error fetching top movies:", textStatus, errorThrown);
            },
        });
    }

    // Add form submission handler
    $("#search-form").on("submit", function (e) {
        e.preventDefault();
        const query = $("#search-input").val().trim();
        if (query.length >= 3) {
            window.location.href = `search-results.html?title=${encodeURIComponent(query)}`;
        } else {
            alert("Please enter at least 3 characters for your search.");
        }
    });

    // Initial fetching of movies
    fetchMovies();
    fetchTopMovies();

    // Auto-complete functionality with caching
    $("#search-input").autocomplete({
        source: function (request, response) {
            const term = request.term.trim();
            if (term.length < 3) {
                console.log("Query too short for autocomplete:", term);
                return;
            }

            if (cache[term]) {
                console.log("Using cached results for:", term);
                response(cache[term]);
                return;
            }

            console.log("Autocomplete search initiated:", term);
            $.ajax({
                url: "api/autocomplete",
                method: "GET",
                data: { query: term },
                success: function (data) {
                    console.log("Caching results for:", term);
                    const suggestions = data.map((item) => ({
                        label: item.value, // Displayed text
                        value: item.data.movieID || item.data.starID, // ID (movie or star)
                        type: item.data.movieID ? "movie" : "star", // Type (movie or star)
                    }));
                    cache[term] = suggestions; // Cache the suggestions
                    response(suggestions);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.error("Error fetching autocomplete suggestions:", textStatus, errorThrown);
                },
            });
        },
        select: function (event, ui) {
            if (ui.item.type === "movie") {
                // Redirect to single-movie.html
                window.location.href = `single-movie.html?id=${encodeURIComponent(ui.item.value)}`;
            } else if (ui.item.type === "star") {
                // Redirect to single-star.html
                window.location.href = `single-star.html?id=${encodeURIComponent(ui.item.value)}`;
            } else {
                console.error("Unexpected selection type:", ui.item);
            }
        },
    });

    // Event listener for the search button
    $("#search-button").click(function () {
        const title = $("#search-input").val().trim();
        if (!title) {
            alert("Please enter a movie title to search.");
            return;
        }
        // Redirect to search-results.html with the query parameter
        window.location.href = `search-results.html?title=${encodeURIComponent(title)}`;
    });

    // Event listener for the "Enter" key in the search input
    $("#search-input").keypress(function (event) {
        if (event.which === 13) { // Enter key
            const title = $("#search-input").val().trim();
            if (!title) {
                alert("Please enter a movie title to search.");
                return;
            }
            // Redirect to search-results.html with the query parameter
            window.location.href = `search-results.html?title=${encodeURIComponent(title)}`;
        }
    });

    // Event listener for changing the number of movies per page
    $('#num-movies').change(function () {
        N = parseInt($(this).val());
        currentPage = 1;
        fetchMovies();
    });

    // Event listener for pagination buttons
    $('#prev-button').click(function () {
        if (currentPage > 1) {
            currentPage--;
            fetchMovies();
        }
    });

    $('#next-button').click(function () {
        currentPage++;
        fetchMovies();
    });

    // Event listener for sorting options
    $('#primary-sort-by, #primary-sort-order, #secondary-sort-by, #secondary-sort-order').change(function () {
        const primarySortBy = $('#primary-sort-by').val();
        const primarySortOrder = $('#primary-sort-order').val();
        const secondarySortBy = $('#secondary-sort-by').val();
        const secondarySortOrder = $('#secondary-sort-order').val();

        fetchMovies({
            primarySortBy,
            primarySortOrder,
            secondarySortBy,
            secondarySortOrder,
        });
    });

    /**
     * Fetch movies from the server
     * @param {Object} params - Additional query parameters
     */
    function fetchMovies(params = {}) {
        const queryParams = { ...getQueryParams(), ...params };
        queryParams.page = currentPage;
        queryParams.N = N;

        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/movies",
            data: queryParams,
            success: (resultData) => {
                handleMovieResult(resultData);
                updatePaginationButtons(resultData.length);
            },
            error: (jqXHR, textStatus, errorThrown) => {
                console.error("AJAX Error:", textStatus, errorThrown);
            },
        });
    }

    /**
     * Handle and render movie data from the server
     * @param {Array} resultData - List of movies
     */
    function handleMovieResult(resultData) {
        const movieTableBodyElement = $("#movie_table_body");
        movieTableBodyElement.empty();

        if (!resultData || resultData.length === 0) {
            console.warn("No movies returned from the server.");
            movieTableBodyElement.append("<tr><td colspan='7'>No movies found.</td></tr>");
            return;
        }

        resultData.forEach((movie) => {
            const posterUrl = movie.posterUrl || "res/images/default-poster.jpg"; // Default poster
            const genres = movie.genres
                ? movie.genres.split(", ").map(
                    (genre) => `<a href="index.html?genre=${genre}">${genre}</a>`
                ).join(", ")
                : "N/A";

            const stars = movie.stars && movie.starIds
                ? movie.stars.split(", ").map(
                    (star, index) =>
                        `<a href='single-star.html?id=${movie.starIds.split(", ")[index]}'>${star}</a>`
                ).join(", ")
                : "N/A";

            const rowHTML = `
            <tr>
                <th><a href='single-movie.html?id=${movie.id}'>${movie.title}</a></th>
                <th>${movie.year || "Unknown"}</th>
                <th>${movie.director || "Unknown"}</th>
                <th>${genres}</th>
                <th>${stars}</th>
                <th>${movie.rating || "N/A"}</th>
                <th>
                    <img src="${posterUrl}" alt="${movie.title}" style="width: 50px; height: 75px;">
                    <button class='add-to-cart' data-movie-id='${movie.id}'>Add to Cart</button>
                </th>
            </tr>`;
            movieTableBodyElement.append(rowHTML);
        });

        $(".add-to-cart").click(function () {
            const movieId = $(this).data("movie-id");
            addToCart(movieId);
        });
    }

    /**
     * Parse query parameters from the URL
     * @returns {Object} - Key-value pairs of query parameters
     */
    function getQueryParams() {
        const urlParams = new URLSearchParams(window.location.search);
        const params = {};
        urlParams.forEach((value, key) => {
            params[key] = value;
        });
        return params;
    }

    /**
     * Update the state of pagination buttons
     * @param {number} resultLength - Number of results in the current page
     */
    function updatePaginationButtons(resultLength) {
        $('#prev-button').prop('disabled', currentPage === 1);
        $('#next-button').prop('disabled', resultLength < N);
    }
});
