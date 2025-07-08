
The tests demonstrate:
- Resource discovery from multiple modules
- OpenAPI specification generation
- Plugin-like behavior without runtime dependencies

## Architecture

```
OpenApiGenerator
├── OpenmrsClassScanner (discovers resources)
├── Resource Loading (validates and instantiates)
├── SwaggerSpecificationCreator (generates spec)
└── File Output (writes JSON)
```