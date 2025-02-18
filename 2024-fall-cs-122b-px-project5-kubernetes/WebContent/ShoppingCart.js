/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleShoppingCartData(resultData){
    console.log("handleShoppingCartData: populating #shoppingcart_table from resultData");
    let shoppingCartTableElement = jQuery("#shoppingcart_table_body");
    let totalCost = jQuery("#total_cost");
    let costExist = false;
    for (let i = 0; i < resultData.length; i++){
        if (i < resultData.length - 1){
            let rowHTML = "";
            rowHTML += "<tr><th>";
            rowHTML +=
                // Add a link to single-movie.html with id passed with GET url parameter
                '<a href="single-movie.html?id=' + resultData[i]['movieId'] + '">'
                + resultData[i]["title"] +     // display movie title for the link text
                '</a>' +
                "</th>";
            rowHTML += "<th>" +
                '<input type="number" class="quantity" data-movie-id="'+ resultData[i]['movieId'] + '" min="1" step="1" placeholder="'+ resultData[i]["quantity"] + '" style="font-weight: bold; border: 1px solid #ccc; border-radius: 5px; padding: 5px 10px;">'
                + "</th>";
            rowHTML += "<th> $" + resultData[i]["price"] + "</th>";
            rowHTML += "<th> $" + resultData[i]["itemTotal"] + "</th>";
            rowHTML += '<th><button class="deleteButton" data-movie-id="' + resultData[i]['movieId'] + '">X</button></th>';            rowHTML += "</tr>";
            shoppingCartTableElement.append(rowHTML);
        }
        else{
            costExist = true;
            totalCost.append(resultData[i].toFixed(2));
            sessionStorage.setItem("totalPrice", resultData[i].toFixed(2));
        }
    }

    if (!costExist){
        console.log("No items");
        totalCost.html("0.00");
        sessionStorage.setItem("totalPrice", "0.00");
    }
}

function deleteItem(movieId){
    // Retrieve the string value associated with the key
    let stringValue = sessionStorage.getItem("cartItems");

    // Parse the string value into an array
    let arrayValue = JSON.parse(stringValue); // Assuming the list is stored as a JSON string

    // Iterate through the array to find and remove elements matching the given string
    let modifiedArray = arrayValue.filter(item => item !== movieId);

    // Update the value associated with the key in sessionStorage with the modified array
    sessionStorage.setItem("cartItems", JSON.stringify(modifiedArray));
}

function updateQuantity(movieId, quantity){
    let stringValue = sessionStorage.getItem("cartItems");
    let arrayValue = JSON.parse(stringValue);
    let modifiedArray = arrayValue.filter(item => item !== movieId);

    while (quantity > 0){
        modifiedArray.push(movieId);
        quantity--;
    }

    sessionStorage.setItem("cartItems", JSON.stringify(modifiedArray));
    location.reload();

}

var cartItems = sessionStorage.getItem("cartItems");

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: 'api/ShoppingCart',
    data: {data : cartItems},
    success: handleShoppingCartData
});

// Delete movies from cart when deleteButton is pressed
document.addEventListener('DOMContentLoaded', function() {
    let shoppingCartTableBody = document.getElementById("shoppingcart_table_body");

    shoppingCartTableBody.addEventListener('click', function(event) {
        // Check if the clicked element is a button with the class 'deleteButton'
        if (event.target && event.target.classList.contains('deleteButton')) {
            // Retrieve the movie ID from the data-movie-id attribute of the button
            let movieId = event.target.getAttribute('data-movie-id');
            // Call the deleteItem function with the movie ID
            deleteItem(movieId);
            location.reload();
        }
    });
});

document.addEventListener('keypress', function(event) {
    // Check if the event target has the class 'page_number_input'
    if (event.target.classList.contains('quantity') && event.key === 'Enter') {
        // Get the entered page number from the event target (the input element)
        let enteredquantity = parseInt(event.target.value);
        let id = event.target.getAttribute('data-movie-id');
        // Validate the entered page number
        if (!isNaN(enteredquantity) && enteredquantity > 0) {
            // Call the function to update the page parameter and navigate to that page
            updateQuantity(id, enteredquantity);
        } else {
            // Optionally, alert the user if the input is not valid
            alert("Please enter a valid non-zero positive integer for the page number.");
        }
    }
});

