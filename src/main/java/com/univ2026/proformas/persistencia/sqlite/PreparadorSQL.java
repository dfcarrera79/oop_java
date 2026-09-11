package com.univ2026.proformas.persistencia.sqlite;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Configura de forma segura los parametros de una sentencia preparada. */
@FunctionalInterface
interface PreparadorSQL {
    void preparar(PreparedStatement sentencia) throws SQLException;
}
