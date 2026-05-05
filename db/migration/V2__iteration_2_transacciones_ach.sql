-- Iteration 2 - HU-12, HU-13, Task 59 y Task 60
-- Ajuste de transacciones para tipo_operacion y flujo ACH.

ALTER TABLE transaccion
    ADD COLUMN IF NOT EXISTS tipo_operacion VARCHAR(40),
    ADD COLUMN IF NOT EXISTS banco_destino VARCHAR(120),
    ADD COLUMN IF NOT EXISTS tipo_cuenta_destino_externa VARCHAR(30),
    ADD COLUMN IF NOT EXISTS numero_cuenta_destino_externa VARCHAR(40),
    ADD COLUMN IF NOT EXISTS tipo_documento_receptor VARCHAR(30),
    ADD COLUMN IF NOT EXISTS numero_documento_receptor VARCHAR(40),
    ADD COLUMN IF NOT EXISTS nombre_receptor_externo VARCHAR(150),
    ADD COLUMN IF NOT EXISTS referencia_externa VARCHAR(80),
    ADD COLUMN IF NOT EXISTS motivo_rechazo VARCHAR(255);

UPDATE transaccion
SET tipo_operacion = tipo
WHERE tipo_operacion IS NULL AND EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'transaccion' AND column_name = 'tipo'
);
