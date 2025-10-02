# Wisemapping API Documentation

Welcome to the Wisemapping REST API documentation. This API provides comprehensive functionality for managing mind maps, users, collaboration, and administrative tasks.

## Table of Contents

- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [Base URL](#base-url)
- [API Endpoints](#api-endpoints)
- [Data Models](#data-models)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)
- [Examples](#examples)
- [SDKs and Tools](#sdks-and-tools)

## Getting Started

The Wisemapping API is a RESTful service that allows you to:

- Create, read, update, and delete mind maps
- Manage user accounts and authentication
- Handle collaboration and sharing
- Organize content with labels
- Perform administrative tasks (admin users only)

### Prerequisites

- A Wisemapping account
- Basic understanding of REST APIs
- Knowledge of JSON and HTTP

## Authentication

The API uses JWT (JSON Web Token) authentication. Here's how to authenticate:

### 1. Get Authentication Token

```bash
curl -X POST "https://your-domain.com/api/restful/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-email@example.com",
    "password": "your-password"
  }'
```

**Response:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Use Token in Requests

Include the token in the Authorization header:

```bash
curl -X GET "https://your-domain.com/api/restful/account" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Token Expiration

JWT tokens have an expiration time (typically 7 days). When a token expires, you'll receive a 401 Unauthorized response. Re-authenticate to get a new token.

## Base URL

The API base URL format is:
```
https://your-domain.com/api/restful
```

For development:
```
http://localhost:8080/api/restful
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/authenticate` | Authenticate user and get JWT token | No |
| POST | `/logout` | Logout user and invalidate token | Yes |

### Account Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/account` | Get current user account info | Yes |
| PUT | `/account/password` | Change user password | Yes |
| PUT | `/account/firstname` | Update first name | Yes |
| PUT | `/account/lastname` | Update last name | Yes |
| PUT | `/account/locale` | Update locale preference | Yes |
| DELETE | `/account` | Delete user account | Yes |

### User Registration

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Register new user | No |
| PUT | `/users/resetPassword` | Reset user password | No |

### Mindmap Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/maps` | Get user's mindmaps | Yes |
| POST | `/maps` | Create new mindmap | Yes |
| GET | `/maps/{id}` | Get mindmap details | Yes |
| PUT | `/maps/{id}` | Update mindmap properties | Yes |
| DELETE | `/maps/{id}` | Delete mindmap | Yes |
| GET | `/maps/{id}/metadata` | Get mindmap metadata | No* |
| PUT | `/maps/{id}/document` | Update mindmap document | Yes |
| GET | `/maps/{id}/document/xml` | Get mindmap XML | Yes |
| PUT | `/maps/{id}/document/xml` | Update mindmap XML | Yes |
| PUT | `/maps/{id}/title` | Update mindmap title | Yes |
| PUT | `/maps/{id}/description` | Update mindmap description | Yes |
| PUT | `/maps/{id}/publish` | Update publish status | Yes |
| GET | `/maps/{id}/starred` | Get starred status | Yes |
| PUT | `/maps/{id}/starred` | Update starred status | Yes |
| PUT | `/maps/{id}/lock` | Lock/unlock mindmap | Yes |
| GET | `/maps/{id}/history` | Get mindmap history | Yes |
| POST | `/maps/{id}/history/{hid}` | Revert to history version | Yes |
| GET | `/maps/{id}/{hid}/document/xml` | Get XML from history | Yes |
| POST | `/maps/{id}` | Duplicate mindmap | Yes |
| DELETE | `/maps/batch` | Delete multiple mindmaps | Yes |
| POST | `/maps/validate-note` | Validate note content | Yes |

*No authentication required for public mindmaps

### Collaboration

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/maps/{id}/collabs` | Get mindmap collaborators | Yes |
| POST | `/maps/{id}/collabs` | Add collaborators | Yes |
| PUT | `/maps/{id}/collabs` | Update collaborators | Yes |
| DELETE | `/maps/{id}/collabs` | Remove collaborator | Yes |

### Labels

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/labels` | Get user labels | Yes |
| POST | `/labels` | Create new label | Yes |
| DELETE | `/labels/{id}` | Delete label | Yes |
| POST | `/maps/{id}/labels` | Add label to mindmap | Yes |
| DELETE | `/maps/{id}/labels/{lid}` | Remove label from mindmap | Yes |

### Admin (Admin Users Only)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/admin/users` | Get all users | Admin |
| POST | `/admin/users` | Create user | Admin |
| GET | `/admin/users/{id}` | Get user by ID | Admin |
| PUT | `/admin/users/{id}` | Update user | Admin |
| DELETE | `/admin/users/{id}` | Delete user | Admin |
| GET | `/admin/users/email/{email}` | Get user by email | Admin |
| PUT | `/admin/users/{id}/password` | Change user password | Admin |
| GET | `/admin/maps` | Get all mindmaps | Admin |
| GET | `/admin/maps/{id}` | Get mindmap by ID | Admin |
| PUT | `/admin/maps/{id}` | Update mindmap | Admin |
| DELETE | `/admin/maps/{id}` | Delete mindmap | Admin |
| GET | `/admin/maps/{id}/xml` | Get mindmap XML | Admin |
| PUT | `/admin/maps/{id}/spam` | Update spam status | Admin |
| GET | `/admin/system/info` | Get system information | Admin |
| GET | `/admin/system/health` | Get system health | Admin |

### Configuration

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/app/config` | Get app configuration | No |

## Data Models

### User

```json
{
  "id": 123,
  "email": "user@example.com",
  "firstname": "John",
  "lastname": "Doe",
  "locale": "en",
  "authenticationType": "DATABASE",
  "admin": false,
  "allowSendEmail": true,
  "creationDate": "2023-01-01T00:00:00Z",
  "suspended": false,
  "active": true
}
```

### Mindmap

```json
{
  "id": 456,
  "title": "My Mind Map",
  "description": "A sample mind map",
  "creator": "user@example.com",
  "creationTime": "2023-01-01T00:00:00Z",
  "lastModificationTime": "2023-01-02T00:00:00Z",
  "lastEditor": "user@example.com",
  "isPublic": false,
  "isLocked": false,
  "lockedBy": null,
  "starred": false,
  "properties": "{\"theme\":\"default\"}",
  "xml": "<map>...</map>"
}
```

### Collaboration

```json
{
  "email": "collaborator@example.com",
  "role": "EDITOR"
}
```

### Label

```json
{
  "id": 789,
  "title": "Important",
  "color": "#ff0000"
}
```

## Error Handling

The API uses standard HTTP status codes and returns detailed error information in JSON format.

### Common Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Request successful, no content returned
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required or invalid
- `403 Forbidden` - Access denied (e.g., admin required)
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Error Response Format

```json
{
  "message": "Error description",
  "code": "ERROR_CODE",
  "details": {
    "field": "Additional error details"
  }
}
```

### Common Error Scenarios

1. **Invalid Authentication**
   ```json
   {
     "message": "Authentication failed",
     "code": "AUTH_FAILED"
   }
   ```

2. **Resource Not Found**
   ```json
   {
     "message": "Map could not be found. Id: 123",
     "code": "MAP_NOT_FOUND"
   }
   ```

3. **Validation Error**
   ```json
   {
     "message": "Validation failed",
     "code": "VALIDATION_ERROR",
     "details": {
       "title": "Title is required"
     }
   }
   ```

## Rate Limiting

API requests are rate limited to prevent abuse. Rate limit information is included in response headers:

- `X-RateLimit-Limit` - Maximum requests per time window
- `X-RateLimit-Remaining` - Remaining requests in current window
- `X-RateLimit-Reset` - Time when the rate limit resets

When rate limited, you'll receive a `429 Too Many Requests` response.

## Examples

### Complete Workflow: Create and Share a Mindmap

#### 1. Authenticate

```bash
curl -X POST "https://your-domain.com/api/restful/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

#### 2. Create a Mindmap

```bash
curl -X POST "https://your-domain.com/api/restful/maps?title=My%20New%20Map" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/xml" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<map>
  <topic text="Central Topic">
    <topic text="Branch 1"/>
    <topic text="Branch 2"/>
  </topic>
</map>'
```

#### 3. Add Collaborators

```bash
curl -X POST "https://your-domain.com/api/restful/maps/123/collabs" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "collaborations": [
      {
        "email": "collaborator@example.com",
        "role": "EDITOR"
      }
    ],
    "message": "Please help me with this mindmap"
  }'
```

#### 4. Make the Mindmap Public

```bash
curl -X PUT "https://your-domain.com/api/restful/maps/123/publish" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isPublic": true
  }'
```

### Working with Labels

#### 1. Create a Label

```bash
curl -X POST "https://your-domain.com/api/restful/labels" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Important",
    "color": "#ff0000"
  }'
```

#### 2. Add Label to Mindmap

```bash
curl -X POST "https://your-domain.com/api/restful/maps/123/labels" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '456'
```

### Admin Operations

#### 1. Get All Users

```bash
curl -X GET "https://your-domain.com/api/restful/admin/users?page=0&pageSize=10" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 2. Get System Health

```bash
curl -X GET "https://your-domain.com/api/restful/admin/system/health" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## SDKs and Tools

### cURL Examples

All examples in this documentation use cURL, which is available on most systems.

### Postman Collection

You can import the API endpoints into Postman for testing:

1. Download the OpenAPI specification
2. Import into Postman
3. Set up environment variables for base URL and authentication token

### JavaScript/Node.js

```javascript
const axios = require('axios');

class WisemappingAPI {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.token = null;
  }

  async authenticate(email, password) {
    const response = await axios.post(`${this.baseUrl}/authenticate`, {
      email,
      password
    });
    this.token = response.data;
    return this.token;
  }

  async getAccount() {
    const response = await axios.get(`${this.baseUrl}/account`, {
      headers: { Authorization: `Bearer ${this.token}` }
    });
    return response.data;
  }

  async createMindmap(title, xml) {
    const response = await axios.post(`${this.baseUrl}/maps?title=${encodeURIComponent(title)}`, xml, {
      headers: {
        Authorization: `Bearer ${this.token}`,
        'Content-Type': 'application/xml'
      }
    });
    return response.headers.location;
  }
}

// Usage
const api = new WisemappingAPI('https://your-domain.com/api/restful');
await api.authenticate('user@example.com', 'password123');
const account = await api.getAccount();
console.log(account);
```

### Python

```python
import requests
import json

class WisemappingAPI:
    def __init__(self, base_url):
        self.base_url = base_url
        self.token = None

    def authenticate(self, email, password):
        response = requests.post(f"{self.base_url}/authenticate", json={
            "email": email,
            "password": password
        })
        self.token = response.text
        return self.token

    def get_account(self):
        headers = {"Authorization": f"Bearer {self.token}"}
        response = requests.get(f"{self.base_url}/account", headers=headers)
        return response.json()

    def create_mindmap(self, title, xml):
        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/xml"
        }
        response = requests.post(
            f"{self.base_url}/maps?title={title}",
            data=xml,
            headers=headers
        )
        return response.headers.get('Location')

# Usage
api = WisemappingAPI('https://your-domain.com/api/restful')
api.authenticate('user@example.com', 'password123')
account = api.get_account()
print(account)
```

## Related Documentation

- **[Backend Documentation](backend/README.md)** - Backend-specific documentation including telemetry and OpenAPI specs
- **[REST Services](backend/rest-api/REST%20Services.md)** - Legacy REST services documentation with CURL examples
- **[Telemetry & Metrics](backend/telemetry/Telemetry.md)** - OpenTelemetry implementation and monitoring
- **[OpenAPI Specifications](backend/openapi-specs/)** - Complete API specifications in OpenAPI format

## Support

For API support and questions:

- **GitHub Issues**: [https://github.com/wisemapping/wisemapping-open-source/issues](https://github.com/wisemapping/wisemapping-open-source/issues)
- **Documentation**: [https://github.com/wisemapping/wisemapping-open-source](https://github.com/wisemapping/wisemapping-open-source)
- **Email**: support@wisemapping.com

## License

This API is licensed under the WiseMapping Public License. See the [LICENSE](https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md) file for details.
