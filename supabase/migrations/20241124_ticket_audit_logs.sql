-- Migración: Tabla de auditoría de modificaciones de tickets
-- Fecha: 24/11/2026
-- Descripción: Registra automáticamente cambios en la tabla tickets mediante triggers
-- Revisión final: 24/11/2026 - security_invoker explícito, search_path vacío, permisos explícitos

-- Tabla de auditoría
-- NOTA: ticket_id NO tiene FK para permitir que los logs persistan incluso si el ticket es eliminado
CREATE TABLE IF NOT EXISTS public.ticket_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    operation TEXT NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    field_name TEXT,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices para consultas rápidas
CREATE INDEX IF NOT EXISTS idx_audit_logs_ticket_id ON public.ticket_audit_logs(ticket_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON public.ticket_audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON public.ticket_audit_logs(user_id);

-- Comentario
COMMENT ON TABLE public.ticket_audit_logs IS 'Registro de auditoría de modificaciones en tickets. Llenado automáticamente por triggers. Los logs persisten incluso si el ticket es eliminado.';

-- ===========================================
-- Triggers para auditoría (SECURITY DEFINER)
-- ===========================================

-- Trigger para INSERT
-- Usa auth.uid() para registrar al usuario autenticado que realmente creó el ticket
-- Maneja caso donde auth.uid() es NULL (sin sesión activa) - no registra
-- NOTA: search_path vacío + nombres explícitos para seguridad
CREATE OR REPLACE FUNCTION public.log_ticket_insert()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    -- Solo registrar si hay un usuario autenticado
    IF auth.uid() IS NOT NULL THEN
        INSERT INTO public.ticket_audit_logs (
            ticket_id,
            user_id,
            operation,
            field_name,
            old_value,
            new_value
        ) VALUES (
            NEW.id,
            auth.uid(),
            'INSERT',
            NULL,
            NULL,
            'Ticket creado'
        );
    END IF;
    RETURN NEW;
END;
$$;

-- Eliminar trigger si existe antes de crear (idempotencia)
DROP TRIGGER IF EXISTS trigger_ticket_insert_audit ON public.tickets;

CREATE TRIGGER trigger_ticket_insert_audit
AFTER INSERT ON public.tickets
FOR EACH ROW
EXECUTE FUNCTION public.log_ticket_insert();

-- Trigger para UPDATE
-- Usa auth.uid() para registrar al usuario autenticado que realizó la modificación
-- Maneja caso donde auth.uid() es NULL - no registra
-- NOTA: search_path vacío + nombres explícitos para seguridad
CREATE OR REPLACE FUNCTION public.log_ticket_update()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    -- Solo registrar si hay un usuario autenticado
    IF auth.uid() IS NOT NULL THEN
        IF OLD.estado IS DISTINCT FROM NEW.estado THEN
            INSERT INTO public.ticket_audit_logs (ticket_id, user_id, operation, field_name, old_value, new_value)
            VALUES (NEW.id, auth.uid(), 'UPDATE', 'estado', OLD.estado, NEW.estado);
        END IF;
        
        IF OLD.prioridad IS DISTINCT FROM NEW.prioridad THEN
            INSERT INTO public.ticket_audit_logs (ticket_id, user_id, operation, field_name, old_value, new_value)
            VALUES (NEW.id, auth.uid(), 'UPDATE', 'prioridad', OLD.prioridad, NEW.prioridad);
        END IF;
        
        IF OLD.descripcion IS DISTINCT FROM NEW.descripcion THEN
            INSERT INTO public.ticket_audit_logs (ticket_id, user_id, operation, field_name, old_value, new_value)
            VALUES (NEW.id, auth.uid(), 'UPDATE', 'descripcion', LEFT(OLD.descripcion, 100), LEFT(NEW.descripcion, 100));
        END IF;
        
        IF OLD.equipo IS DISTINCT FROM NEW.equipo THEN
            INSERT INTO public.ticket_audit_logs (ticket_id, user_id, operation, field_name, old_value, new_value)
            VALUES (NEW.id, auth.uid(), 'UPDATE', 'equipo', OLD.equipo, NEW.equipo);
        END IF;
        
        IF OLD.tipo_incidencia IS DISTINCT FROM NEW.tipo_incidencia THEN
            INSERT INTO public.ticket_audit_logs (ticket_id, user_id, operation, field_name, old_value, new_value)
            VALUES (NEW.id, auth.uid(), 'UPDATE', 'tipo_incidencia', OLD.tipo_incidencia, NEW.tipo_incidencia);
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

-- Eliminar trigger si existe antes de crear (idempotencia)
DROP TRIGGER IF EXISTS trigger_ticket_update_audit ON public.tickets;

CREATE TRIGGER trigger_ticket_update_audit
AFTER UPDATE ON public.tickets
FOR EACH ROW
EXECUTE FUNCTION public.log_ticket_update();

-- Trigger para DELETE
-- Usa auth.uid() para registrar al usuario autenticado que eliminó el ticket
-- NO tiene FK en ticket_id, por lo que puede insertar aunque el ticket sea eliminado
-- Maneja caso donde auth.uid() es NULL - no registra
-- NOTA: search_path vacío + nombres explícitos para seguridad
CREATE OR REPLACE FUNCTION public.log_ticket_delete()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    -- Solo registrar si hay un usuario autenticado
    IF auth.uid() IS NOT NULL THEN
        INSERT INTO public.ticket_audit_logs (
            ticket_id,
            user_id,
            operation,
            field_name,
            old_value,
            new_value
        ) VALUES (
            OLD.id,
            auth.uid(),
            'DELETE',
            NULL,
            NULL,
            'Ticket eliminado'
        );
    END IF;
    RETURN OLD;
END;
$$;

-- Eliminar trigger si existe antes de crear (idempotencia)
DROP TRIGGER IF EXISTS trigger_ticket_delete_audit ON public.tickets;

CREATE TRIGGER trigger_ticket_delete_audit
AFTER DELETE ON public.tickets
FOR EACH ROW
EXECUTE FUNCTION public.log_ticket_delete();

-- ===========================================
-- RLS (Row Level Security)
-- ===========================================

ALTER TABLE public.ticket_audit_logs ENABLE ROW LEVEL SECURITY;

-- Eliminar políticas si existen antes de crear (idempotencia)
DROP POLICY IF EXISTS "Solo admins pueden leer audit logs" ON public.ticket_audit_logs;
DROP POLICY IF EXISTS "Nadie puede insertar audit logs manualmente" ON public.ticket_audit_logs;
DROP POLICY IF EXISTS "Nadie puede actualizar audit logs" ON public.ticket_audit_logs;
DROP POLICY IF EXISTS "Nadie puede eliminar audit logs" ON public.ticket_audit_logs;

CREATE POLICY "Solo admins pueden leer audit logs"
ON public.ticket_audit_logs FOR SELECT
USING (EXISTS (SELECT 1 FROM public.profiles WHERE profiles.id = auth.uid() AND profiles.rol = 'admin'));

CREATE POLICY "Nadie puede insertar audit logs manualmente" ON public.ticket_audit_logs FOR INSERT WITH CHECK (false);
CREATE POLICY "Nadie puede actualizar audit logs" ON public.ticket_audit_logs FOR UPDATE WITH CHECK (false);
CREATE POLICY "Nadie puede eliminar audit logs" ON public.ticket_audit_logs FOR DELETE WITH CHECK (false);

-- ===========================================
-- Función auxiliar para obtener nombre de usuario (SECURITY DEFINER)
-- ===========================================
-- Permite recuperar solo el nombre de un perfil específico sin exponer toda la tabla profiles
-- Esta función bypass RLS pero está restringida a una sola columna por ID
-- NOTA: Solo administradores pueden usarla (verifica profiles.rol = 'admin')
CREATE OR REPLACE FUNCTION public.get_user_name(user_id UUID)
RETURNS TEXT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    -- Verificar que hay sesión activa
    IF auth.uid() IS NULL THEN
        RETURN NULL;
    END IF;

    -- Verificar que el usuario que invoca la función es administrador
    IF NOT EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND rol = 'admin'
    ) THEN
        RETURN NULL;
    END IF;

    -- Solo si es administrador, devolver el nombre del perfil solicitado
    RETURN (
        SELECT nombre FROM public.profiles
        WHERE id = user_id LIMIT 1
    );
END;
$$;

-- Permisos para la función
-- Revocar EXECUTE de PUBLIC y anon para restringir acceso
REVOKE EXECUTE ON FUNCTION public.get_user_name(UUID) FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION public.get_user_name(UUID) FROM anon;

-- Conceder EXECUTE solo a authenticated (la función ya verifica que sea admin)
GRANT EXECUTE ON FUNCTION public.get_user_name(UUID) TO authenticated;

-- ===========================================
-- Permisos para triggers y vistas
-- ===========================================

-- Permisos de esquema
GRANT USAGE ON SCHEMA public TO anon, authenticated;

-- Permisos sobre ticket_audit_logs
-- NOTA: INSERT eliminado porque las inserciones deben ser por triggers SECURITY DEFINER
GRANT SELECT ON public.ticket_audit_logs TO authenticated;

-- ===========================================
-- Vista para auditoría con nombre de usuario
-- ===========================================
-- NOTA: security_invoker = true explícito para respetar RLS de la tabla subyacente
-- Solo admins pueden ver la vista debido a la política SELECT en la tabla
-- Usa función get_user_name() para evitar GRANT SELECT directo sobre profiles

-- Eliminar vista si existe antes de crear (idempotencia)
DROP VIEW IF EXISTS public.ticket_audit_logs_with_user;

CREATE VIEW public.ticket_audit_logs_with_user
WITH (security_invoker = true)
AS
SELECT
    l.id,
    l.ticket_id,
    l.user_id,
    public.get_user_name(l.user_id) AS user_name,
    l.operation,
    l.field_name,
    l.old_value,
    l.new_value,
    l.created_at
FROM public.ticket_audit_logs l;

-- Comentario de la vista
COMMENT ON VIEW public.ticket_audit_logs_with_user IS 'Vista de auditoría con nombre de usuario. Úsala para SELECT en lugar de la tabla directa. Solo administradores pueden verla debido a RLS (security_invoker = true). Usa función get_user_name() para evitar acceso directo a profiles.';

-- Permisos para la vista
GRANT SELECT ON public.ticket_audit_logs_with_user TO authenticated;
