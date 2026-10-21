# Frontend — Mesa (Sulwork)

SPA em React + Vite + TypeScript para organizar eventos de alimentação.

```bash
npm install
npm run dev
```

A API local fica em `http://localhost:8080/api/v1`. Para apontar para outro backend, copie `.env.example` para `.env` e ajuste `VITE_API_URL`.

```bash
npm test
npm run build
```

Imagem Docker (nginx servindo o `dist`):

```bash
docker build -t mesa-frontend --build-arg VITE_API_URL=https://desafio-sulwork.onrender.com/api/v1 .
```
