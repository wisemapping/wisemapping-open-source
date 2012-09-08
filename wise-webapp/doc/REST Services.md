REST Services
---------------

Obtaining user information by email:

curl "http://localhost:8080/service/admin/users/email/{user.email}.json" --get --basic -u "admin@wisemapping.org:admin"

Deleting a based on the user id:
curl "http://localhost:8080/service/admin/users/{userId}" --request delete --basic -u "admin@wisemapping.org:admin"


