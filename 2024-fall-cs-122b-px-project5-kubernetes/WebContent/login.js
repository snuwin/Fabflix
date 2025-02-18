new Vue({
    el: '#app',
    data : {
        form : {
            email: '',
            password: '',
        },
        info: {
            status: '',
            message: ''
        }
    },
    methods : {
        handleSubmit() {
            axios
                .post('api/login', this.form) // Send form data to the API
                .then((response) => {
                    this.info = response.data; // Handle API response
                    if (this.info.status == 'success') {
                        window.location.href = 'index.html'; // Redirect on successful login
                    } else {
                        // Show the error message from the response
                        document.getElementById("login_error_message").textContent = this.info.message || 'Login failed. Please try again.';
                    }
                })
                .catch((error) => {
                    console.error("Login error:", error);
                    this.info.status = 'fail';
                    this.info.message = 'An error occurred. Please try again.';
                    document.getElementById("login_error_message").textContent = this.info.message;
                });
        }
    }
});