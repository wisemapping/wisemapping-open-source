# Wisemapping Backend Documentation

This directory contains comprehensive documentation for the Wisemapping backend API and services.

## 📁 Directory Structure

```
backend/
├── README.md                    # This file - Backend documentation index
├── rest-api/                    # REST API documentation
│   └── REST Services.md        # Legacy REST services documentation
├── telemetry/                   # Telemetry and metrics documentation
│   └── Telemetry.md            # OpenTelemetry and metrics implementation
└── openapi-specs/              # OpenAPI specifications
    ├── openapi.yaml            # Main OpenAPI specification
    ├── mindmap-endpoints.yaml  # Mindmap-specific endpoints
    └── collaboration-admin-endpoints.yaml # Admin and collaboration endpoints
```

## 📚 Documentation Overview

### REST API Documentation
- **[REST Services](rest-api/REST%20Services.md)** - Legacy documentation for REST services with CURL examples
- **[Main API Documentation](../README.md)** - Comprehensive API documentation with examples

### Telemetry & Monitoring
- **[Telemetry & Metrics](telemetry/Telemetry.md)** - OpenTelemetry implementation, metrics tracking, and monitoring setup

### API Specifications
- **[OpenAPI Specification](openapi-specs/openapi.yaml)** - Complete API specification in OpenAPI 3.0 format
- **[Mindmap Endpoints](openapi-specs/mindmap-endpoints.yaml)** - Mindmap-specific API endpoints
- **[Admin & Collaboration](openapi-specs/collaboration-admin-endpoints.yaml)** - Administrative and collaboration endpoints

## 🚀 Quick Start

1. **For API Integration**: Start with the [Main API Documentation](../README.md)
2. **For OpenAPI Tools**: Use the [OpenAPI Specification](openapi-specs/openapi.yaml)
3. **For Monitoring**: Check the [Telemetry Documentation](telemetry/Telemetry.md)
4. **For Legacy Integration**: Refer to [REST Services](rest-api/REST%20Services.md)

## 🔧 Development

### Adding New Documentation

When adding new backend documentation:

1. **API Documentation**: Add to the main [README.md](../README.md)
2. **OpenAPI Specs**: Update the appropriate YAML files in `openapi-specs/`
3. **Telemetry**: Update [Telemetry.md](telemetry/Telemetry.md) for metrics changes
4. **Legacy Docs**: Update [REST Services.md](rest-api/REST%20Services.md) for legacy endpoints

### Documentation Standards

- Use Markdown format for all documentation
- Include code examples where applicable
- Keep OpenAPI specifications up to date
- Document all new endpoints and changes
- Include authentication requirements
- Provide error handling examples

## 📖 Related Documentation

- [Frontend Documentation](../../../wisemapping-frontend/README.md) - Frontend application documentation
- [Database Configuration](../../../config/database/) - Database setup and migration scripts
- [Deployment Guide](../../../distribution/) - Docker and deployment documentation

## 🤝 Contributing

When contributing to the backend documentation:

1. Follow the existing structure and format
2. Update relevant sections when making API changes
3. Include examples for new endpoints
4. Test all code examples
5. Update the OpenAPI specifications

## 📞 Support

For questions about the backend API:
- **GitHub Issues**: [https://github.com/wisemapping/wisemapping-open-source/issues](https://github.com/wisemapping/wisemapping-open-source/issues)
- **Documentation**: This directory and related files
- **Email**: support@wisemapping.com
