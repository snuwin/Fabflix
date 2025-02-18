new Vue({
    el: '#app',
    data: {
        form: {
            email: '',
            password: ''
        },
        info: {
            status: '',
            message: ''
        }
    },
    methods: {
        handleEmployeeLogin() {
            const contextPath = window.location.pathname.split('/')[1];
            const loginUrl = `/${contextPath}/api/employeeLogin`;

            axios.post(loginUrl, this.form)
                .then(response => {
                    this.info = response.data;
                    if (this.info.status === 'success') {
                        window.location.href = `/${contextPath}/_dashboard.html`;
                    } else {
                        document.getElementById("error_message").textContent = this.info.message;
                    }
                })
                .catch(error => {
                    console.error("Login error:", error);
                    this.info.status = 'fail';
                    this.info.message = 'An error occurred. Please try again.';
                    document.getElementById("error_message").textContent = this.info.message;
                });
        }
    }
});
