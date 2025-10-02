# WiseMapping Documentation

Welcome to the WiseMapping documentation hub. This directory contains all project documentation organized by category.

## 📁 Documentation Structure

```
doc/
├── README.md                           # This file - Documentation index
└── api-documentation/                  # API and backend documentation
    ├── README.md                       # Main API documentation
    └── backend/                        # Backend-specific documentation
        ├── README.md                   # Backend documentation index
        ├── rest-api/                   # REST API documentation
        ├── telemetry/                  # Telemetry and metrics
        └── openapi-specs/              # OpenAPI specifications
```

## 🚀 Quick Navigation

### For Developers
- **[API Documentation](api-documentation/README.md)** - Complete REST API documentation with examples
- **[Backend Documentation](api-documentation/backend/README.md)** - Backend-specific documentation
- **[OpenAPI Specifications](api-documentation/backend/openapi-specs/)** - API specifications in OpenAPI format

### For System Administrators
- **[Database Configuration](../config/database/)** - Database setup and migration scripts
- **[Deployment Guide](../distribution/)** - Docker and deployment documentation
- **[Telemetry & Metrics](api-documentation/backend/telemetry/Telemetry.md)** - Monitoring and observability

### For Integration
- **[REST Services](api-documentation/backend/rest-api/REST%20Services.md)** - Legacy REST services with CURL examples
- **[API Examples](api-documentation/README.md#examples)** - Complete workflow examples

## 📚 Documentation Categories

### API Documentation
Complete documentation for the WiseMapping REST API including:
- Authentication and authorization
- Endpoint reference
- Data models and schemas
- Error handling
- Code examples in multiple languages

### Backend Documentation
Backend-specific documentation including:
- Telemetry and metrics implementation
- OpenAPI specifications
- Legacy REST services
- Development guidelines

### System Documentation
- Database configuration and migrations
- Docker deployment guides
- Configuration options
- Monitoring and observability

## 🔧 Contributing to Documentation

When adding or updating documentation:

1. **API Changes**: Update the main [API Documentation](api-documentation/README.md)
2. **OpenAPI Specs**: Update files in [openapi-specs/](api-documentation/backend/openapi-specs/)
3. **Backend Features**: Update [Backend Documentation](api-documentation/backend/README.md)
4. **Telemetry**: Update [Telemetry Documentation](api-documentation/backend/telemetry/Telemetry.md)

### Documentation Standards
- Use clear, concise language
- Include code examples where applicable
- Keep links up to date
- Follow the existing structure and format
- Test all code examples

## 📞 Support

For documentation questions or improvements:
- **GitHub Issues**: [https://github.com/wisemapping/wisemapping-open-source/issues](https://github.com/wisemapping/wisemapping-open-source/issues)
- **Email**: support@wisemapping.com

## 📄 License

This documentation is licensed under the WiseMapping Public License. See the [LICENSE](../LICENSE.md) file for details.
