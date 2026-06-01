# Examen Práctico — Mantenimiento de Software
## Sistema de Gestión de Videojuegos 🎮
**Facultad de Ingeniería de Sistemas — EPN | Construcción de Software 2026**

---

## Estructura del proyecto

```
/
├── epn-event-manager/              ← Backend NestJS (4 mantenimientos aplicados)
│   ├── src/
│   │   ├── modules/events/
│   │   │   ├── events.service.ts  ← ARCHIVO MODIFICADO (los 4 mantenimientos)
│   │   │   ├── events.controller.ts
│   │   │   └── dto/create-event.dto.ts
│   │   ├── modules/health/
│   │   ├── modules/stats/
│   │   └── database/entities/
│   └── db/events.sqlite
│
└── videojuegos-crud/               ← CRUD del tema elegido
    ├── videojuegos-crud-web.html   ← APP WEB MEJORADA (Examen Práctico)
    ├── src/videojuegos/            ← Versión Java de consola (Taller 3)
    │   ├── Main.java
    │   ├── Videojuego.java
    │   ├── VideojuegosCRUD.java
    │   └── EventClient.java
    ├── compile-and-run.bat         ← Compilar y correr Java en Windows
    └── compile-and-run.sh          ← Compilar y correr Java en Linux/Mac
```

---

## Cómo usar la app web

1. Abrir el archivo `videojuegos-crud/videojuegos-crud-web.html` directamente en el navegador
2. La app funciona completamente en modo local (sin hub)
3. Para conectar con el hub, primero levantar el `epn-event-manager`

---

## Cómo levantar el EPN Event Manager

```bash
cd epn-event-manager
npm install
npm run start:dev
# Servidor en http://localhost:3000
```

Una vez corriendo, los botones "Probar conexión" y "Ver stats" en la app web funcionarán.

---

## Los 4 Mantenimientos aplicados

### Correctivo — `events.service.ts`
El bloque DELETE creaba el objeto en memoria pero nunca llamaba `save()`.
El evento desaparecía silenciosamente con respuesta `{ ok: true }`.

**Antes:** `this.deleteRepo.create({...});`
**Después:** `const ev = this.deleteRepo.create({...}); await this.deleteRepo.save(ev);`

### Adaptativo — `events.service.ts` + app web
Las fechas usaban `toLocaleString()` (dependiente del SO).
Se cambió a `toISOString()` (ISO-8601 UTC universal).
La app web implementa header `X-FIS-EPN-KEY` en cada petición al hub
y panel de variables de entorno (`.env`) editables sin recargar.

### Perfectivo — `events.service.ts` + app web
- `getStats()` no contaba eventos QUERY en el total → corregido
- `findAll()` ordenaba con `localeCompare()` (lexicográfico) → cambiado a `Date.parse()`
- App web: suite de 10 pruebas unitarias ejecutables en vivo
- App web: documentación OpenAPI de todos los endpoints
- App web: exportar logs estructurados a archivo `.txt`

### Preventivo — `events.service.ts` + app web
- Validación de campos obligatorios con `BadRequestException`
- Límite de 100 caracteres en campos texto
- `try-catch` global que captura errores inesperados de BD
- App web: validación completa de formulario (duplicados, rangos, tipos)
- App web: constantes centralizadas en objeto `CONFIG`

---

## Prueba de vida

1. Abrir `videojuegos-crud-web.html` en el navegador
2. Usar el CRUD: crear, editar, eliminar videojuegos
3. Ver los logs ISO-8601 generarse en tiempo real en el panel lateral
4. Ejecutar las pruebas unitarias con el botón "▶ Ejecutar"
5. (Opcional) Levantar el hub y probar la conexión con la API-Key
