# Taller de Construcción y Evolución de Software
## Sistema de Gestión de Videojuegos 🎮
**Facultad de Ingeniería de Sistemas – EPN**

---

## Estructura del Repositorio

```
/
├── epn-event-manager/          ← Backend NestJS (modificado con los 4 mantenimientos)
│   └── src/modules/events/
│       ├── events.service.ts   ← ARCHIVO PRINCIPAL MODIFICADO
│       └── dto/create-event.dto.ts
│
└── videojuegos-crud/           ← CRUD Java de consola (tema elegido)
    ├── src/videojuegos/
    │   ├── Main.java           ← Menú principal
    │   ├── Videojuego.java     ← Modelo de datos
    │   ├── VideojuegosCRUD.java← Lógica CRUD
    │   └── EventClient.java    ← Integración con EPN Event Manager
    ├── compile-and-run.bat     ← Para Windows
    └── compile-and-run.sh      ← Para Linux/Mac
```

---

## Fase 1 – CRUD de Videojuegos (Java puro)

**Requisitos:** Solo JDK 11 o superior. Sin frameworks, sin instalaciones extras.

### Cómo ejecutar

**Windows:**
```
cd videojuegos-crud
compile-and-run.bat
```

**Linux / Mac:**
```bash
cd videojuegos-crud
chmod +x compile-and-run.sh
./compile-and-run.sh
```

El CRUD incluye 5 videojuegos pre-cargados y permite:
- Listar todos los juegos
- Buscar por ID
- Agregar nuevos juegos
- Actualizar datos
- Eliminar juegos

Cada operación envía automáticamente un evento `POST /events` al EPN Event Manager.

---

## Fase 2 – Los 4 Mantenimientos aplicados al EPN Event Manager

El archivo clave modificado es: `epn-event-manager/src/modules/events/events.service.ts`

### 🐞 1. Mantenimiento Correctivo

**Problema encontrado:** El bloque `DELETE` creaba el objeto en memoria pero nunca llamaba `save()`. El evento se "perdía" silenciosamente y el método devolvía `{ ok: true }` sin haber guardado nada en la base de datos.

**Código antes:**
```typescript
if (action === 'DELETE') {
  this.deleteRepo.create({ ... });  // ← objeto creado pero no guardado
  return { ok: true };              // ← devuelve éxito sin persistir
}
```

**Código después:**
```typescript
if (action === 'DELETE') {
  const ev = this.deleteRepo.create({ ... });  // ← asignado a variable
  await this.deleteRepo.save(ev);              // ← ahora sí se persiste
  return { ok: true };
}
```

---

### ⚙️ 2. Mantenimiento Adaptativo

**Problema encontrado:** Las fechas se guardaban con `new Date().toLocaleString()`, un formato que depende de la configuración regional del servidor y no es reconocido por sistemas externos ni bases de datos remotas.

**Código antes:**
```typescript
const localDate = new Date().toLocaleString();
// Resultado ejemplo: "5/5/2026, 10:30:00 AM" (varía según el SO)
```

**Código después:**
```typescript
const isoDate = new Date().toISOString();
// Resultado ejemplo: "2026-05-05T15:30:00.000Z" (estándar ISO-8601 UTC)
```

---

### 📈 3. Mantenimiento Perfectivo

Se realizaron **dos mejoras**:

**Mejora A – `getStats()` incompleto:**
Los eventos de tipo QUERY no se contabilizaban en el total.

```typescript
// Antes:
return { create, update, delete, total: create + update + delete };

// Después:
const queryCount = await this.queryRepo.count();
return { create, update, delete, query: queryCount, total: create + update + delete + queryCount };
```

**Mejora B – Ordenamiento de `findAll()` incorrecto:**
Se usaba `localeCompare()` para comparar strings de fecha, lo que produce orden lexicográfico incorrecto.

```typescript
// Antes:
return ta.localeCompare(tb);   // orden de texto, no cronológico

// Después:
return Date.parse(ta) - Date.parse(tb);  // orden numérico real en milisegundos
```

---

### 🛡️ 4. Mantenimiento Preventivo

Se agregaron tres capas de protección:

1. **Validación de campos obligatorios:** Si falta `action`, `source` o `entity`, se lanza `BadRequestException`.
2. **Límite de longitud:** Los campos texto tienen un máximo de 100 caracteres.
3. **Try-catch global:** Cualquier error inesperado (BD no disponible, dato malformado) es capturado y devuelto como error HTTP legible, sin exponer el stack trace.

```typescript
// Ejemplo de validación:
if (!dto.action || !dto.source || !dto.entity) {
  throw new BadRequestException('Los campos action, source y entity son obligatorios.');
}
if (dto.source.length > 100) {
  throw new BadRequestException('source no puede superar 100 caracteres.');
}
```

---

## Cómo levantar el EPN Event Manager

```bash
cd epn-event-manager
npm install
npm run start:dev
```

El servidor queda en `http://localhost:3000`.

Endpoints disponibles:
- `POST /events` – Registrar evento
- `GET /events` – Listar todos los eventos
- `GET /stats` – Ver estadísticas por tipo
- `GET /health` – Estado del servidor
