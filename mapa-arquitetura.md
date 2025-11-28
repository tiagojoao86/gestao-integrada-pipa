┌────────────────────────────────────────────┐
│  Container Nginx SSL (porta 443/80)       │  ← Novo container
│  - Termina SSL/HTTPS                      │
│  - Redireciona HTTP → HTTPS               │
│  - Certbot para Let's Encrypt             │
│  - Proxy / → frontend:80                  │
│  - Proxy /api/ → backend:8080             │
└────────────────────────────────────────────┘
           │                   │
           ↓                   ↓
┌──────────────────┐   ┌─────────────────┐
│ Container        │   │ Container       │
│ Frontend         │   │ Backend         │
│ (nginx interno)  │   │ (Spring Boot)   │
└──────────────────┘   └─────────────────┘