document.addEventListener('DOMContentLoaded', function() {
    const totalPrice = sessionStorage.getItem("totalPrice") || "0.00";
    document.getElementById("total_price_display").innerText = totalPrice;

    const form = document.getElementById("payment_form");

    form.addEventListener("submit", function(event) {
        event.preventDefault(); // Prevents the form from submitting the default way

        // Make sure to get the value from each input field
        const firstName = document.getElementById("first_name").value.trim();
        const lastName = document.getElementById("last_name").value.trim();
        const cardNumber = document.getElementById("card_number").value.trim();
        const expirationDate = document.getElementById("expiration_date").value.trim();

        // Debugging: Log the values to verify they are correctly captured
        console.log({
            first_name: firstName,
            last_name: lastName,
            card_number: cardNumber,
            expiration_date: expirationDate
        });

        // AJAX POST request to the /processPayment endpoint
        fetch('/api/processPayment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                first_name: firstName,
                last_name: lastName,
                card_number: cardNumber,
                expiration_date: expirationDate
            })
        })
            .then(response => {
                if (!response.ok) {  // Check if the response is not OK (e.g., 404 or 500)
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    window.location.href = "ConfirmationPage.html";
                } else {
                    showError(data.errorMessage);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showError("An error occurred. Please try again.");
            });
    });

    function showError(message) {
        const errorMessage = document.getElementById("error_message");
        errorMessage.textContent = message;
        errorMessage.style.display = "block";
    }
});
