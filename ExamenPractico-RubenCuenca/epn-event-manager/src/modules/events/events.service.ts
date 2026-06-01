import { Injectable, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CreateEventDto } from './dto/create-event.dto';
import { CreateEventEntity } from '../../database/entities/create-event.entity';
import { UpdateEventEntity } from '../../database/entities/update-event.entity';
import { DeleteEventEntity } from '../../database/entities/delete-event.entity';
import { QueryEventEntity } from '../../database/entities/query-event.entity';

// =============================================================================
// RESUMEN DE MANTENIMIENTOS APLICADOS
// =============================================================================
// 1. CORRECTIVO  : El bloque DELETE nunca llamaba await + save(). El evento
//                  se creaba en memoria pero jamás se persistía en la BD.
//                  Corrección: se añadió la variable ev y await deleteRepo.save(ev).
//
// 2. ADAPTATIVO  : Las fechas se guardaban con toLocaleString() (formato local,
//                  no estándar, varía según el servidor). Se cambió a
//                  new Date().toISOString() que produce UTC ISO-8601, garantizando
//                  interoperabilidad con cualquier cliente o sistema externo.
//
// 3. PERFECTIVO   : getStats() omitía el conteo de query_events del total.
//                  Se añadió queryCount y se corrigió la suma total.
//                  Además findAll() ahora ordena por el campo de fecha usando
//                  Date.parse() para comparación numérica real, no lexicográfica.
//
// 4. PREVENTIVO  : Se añadió try-catch global en registerEvent(), validación
//                  de action desconocida con BadRequestException, y límites de
//                  longitud para los campos source, entity y title (máx 100 chars).
// =============================================================================

@Injectable()
export class EventsService {
  constructor(
    @InjectRepository(CreateEventEntity)
    private readonly createRepo: Repository<CreateEventEntity>,
    @InjectRepository(UpdateEventEntity)
    private readonly updateRepo: Repository<UpdateEventEntity>,
    @InjectRepository(DeleteEventEntity)
    private readonly deleteRepo: Repository<DeleteEventEntity>,
    @InjectRepository(QueryEventEntity)
    private readonly queryRepo: Repository<QueryEventEntity>,
  ) {}

  async registerEvent(dto: CreateEventDto): Promise<{ ok: boolean; message?: string }> {
    // -------------------------------------------------------------------------
    // MANTENIMIENTO PREVENTIVO: validaciones de entrada antes de procesar
    // -------------------------------------------------------------------------
    try {
      if (!dto.action || !dto.source || !dto.entity) {
        throw new BadRequestException(
          'Los campos action, source y entity son obligatorios.',
        );
      }

      const MAX_LEN = 100;
      if (dto.source.length > MAX_LEN || dto.entity.length > MAX_LEN) {
        throw new BadRequestException(
          `source y entity no pueden superar ${MAX_LEN} caracteres.`,
        );
      }
      if (dto.title && dto.title.length > MAX_LEN) {
        throw new BadRequestException(
          `title no puede superar ${MAX_LEN} caracteres.`,
        );
      }

      const action = dto.action.trim().toUpperCase();
      const payloadStr = JSON.stringify(dto.payload ?? {});

      // -----------------------------------------------------------------------
      // MANTENIMIENTO ADAPTATIVO: fecha en formato ISO-8601 UTC
      // Antes: new Date().toLocaleString()  → depende del locale del servidor
      // Ahora: new Date().toISOString()     → estándar universal UTC
      // -----------------------------------------------------------------------
      const isoDate = new Date().toISOString();

      if (action === 'CREATE') {
        const ev = this.createRepo.create({
          source: dto.source,
          entity: dto.entity,
          action: dto.action,
          title: dto.title,
          description: dto.description,
          payload: payloadStr,
          recorded_at: isoDate,
        });
        await this.createRepo.save(ev);
        return { ok: true };
      }

      if (action === 'UPDATE') {
        const ev = this.updateRepo.create({
          source: dto.source,
          entity: dto.entity,
          action: dto.action,
          title: dto.title,
          description: dto.description,
          payload: payloadStr,
          timestamp: isoDate,
        });
        await this.updateRepo.save(ev);
        return { ok: true };
      }

      if (action === 'DELETE') {
        // -----------------------------------------------------------------------
        // MANTENIMIENTO CORRECTIVO: faltaba asignar el resultado de .create()
        // a una variable y llamar await .save(). El evento DELETE nunca se
        // guardaba en la base de datos.
        // Antes:  this.deleteRepo.create({...});  return { ok: true };
        // Ahora:  const ev = ...; await this.deleteRepo.save(ev);
        // -----------------------------------------------------------------------
        const ev = this.deleteRepo.create({
          source: dto.source,
          entity: dto.entity,
          action: dto.action,
          title: dto.title,
          payload: payloadStr,
          createdAt: isoDate,
        });
        await this.deleteRepo.save(ev);
        return { ok: true };
      }

      if (action === 'QUERY') {
        const ev = this.queryRepo.create({
          source: dto.source,
          entity: dto.entity,
          action: dto.action,
          title: dto.title,
          description: dto.description,
          payload: payloadStr,
          event_date: isoDate,
        });
        await this.queryRepo.save(ev);
        return { ok: true };
      }

      // PREVENTIVO: acción no reconocida → respuesta clara al cliente
      throw new BadRequestException(
        `Acción desconocida: "${dto.action}". Use CREATE, UPDATE, DELETE o QUERY.`,
      );

    } catch (error) {
      // PREVENTIVO: captura cualquier error inesperado (BD caída, dato inválido, etc.)
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException(
        `Error interno al registrar el evento: ${(error as Error).message}`,
      );
    }
  }

  async findAll(): Promise<object[]> {
    const creates = await this.createRepo.find();
    const updates = await this.updateRepo.find();
    const deletes = await this.deleteRepo.find();
    const queries = await this.queryRepo.find();

    const merged = [
      ...creates.map((e) => ({ ...e, _table: 'create_events' })),
      ...updates.map((e) => ({ ...e, _table: 'update_events' })),
      ...deletes.map((e) => ({ ...e, _table: 'delete_events' })),
      ...queries.map((e) => ({ ...e, _table: 'query_events' })),
    ];

    // -------------------------------------------------------------------------
    // MANTENIMIENTO PERFECTIVO: ordenamiento por fecha real (numérico)
    // Antes: ta.localeCompare(tb) → orden lexicográfico, incorrecto para fechas
    // Ahora: Date.parse() convierte ISO-8601 a ms epoch → comparación exacta
    // -------------------------------------------------------------------------
    merged.sort((a, b) => {
      const ra = a as unknown as Record<string, string>;
      const rb = b as unknown as Record<string, string>;
      const ta = ra.recorded_at ?? ra.timestamp ?? ra.createdAt ?? ra.event_date ?? '';
      const tb = rb.recorded_at ?? rb.timestamp ?? rb.createdAt ?? rb.event_date ?? '';
      return Date.parse(ta) - Date.parse(tb);
    });

    return merged;
  }

  async findBySource(source: string): Promise<object[]> {
    const creates = await this.createRepo.findBy({ source });
    const updates = await this.updateRepo.findBy({ source });
    const deletes = await this.deleteRepo.findBy({ source });
    const queries = await this.queryRepo.findBy({ source });
    return [...creates, ...updates, ...deletes, ...queries];
  }

  async findByEntity(entity: string): Promise<object[]> {
    const creates = await this.createRepo.findBy({ entity });
    const updates = await this.updateRepo.findBy({ entity });
    const deletes = await this.deleteRepo.findBy({ entity });
    const queries = await this.queryRepo.findBy({ entity });
    return [...creates, ...updates, ...deletes, ...queries];
  }

  async getStats(): Promise<object> {
    const createCount = await this.createRepo.count();
    const updateCount = await this.updateRepo.count();
    const deleteCount = await this.deleteRepo.count();
    // -------------------------------------------------------------------------
    // MANTENIMIENTO PERFECTIVO: query_events se omitía del total.
    // Antes:  total: createCount + updateCount + deleteCount
    // Ahora:  se incluye queryCount en el conteo y en el total
    // -------------------------------------------------------------------------
    const queryCount = await this.queryRepo.count();
    return {
      create: createCount,
      update: updateCount,
      delete: deleteCount,
      query: queryCount,
      total: createCount + updateCount + deleteCount + queryCount,
    };
  }
}
