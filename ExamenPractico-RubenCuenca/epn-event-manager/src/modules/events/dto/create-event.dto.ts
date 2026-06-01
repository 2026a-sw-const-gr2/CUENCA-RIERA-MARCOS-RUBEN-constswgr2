// MANTENIMIENTO PREVENTIVO: DTO con tipos explícitos para evitar datos inesperados
export class CreateEventDto {
  source: string;
  entity: string;
  action: string;
  title: string;
  description: string;
  payload: Record<string, unknown>;
}
