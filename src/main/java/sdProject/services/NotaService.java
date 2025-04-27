package sdProject.services;

import java.sql.SQLException;


public interface NotaService {
    boolean registrarNota(int alunoId, int disciplinaId, Double nota) throws SQLException, IllegalArgumentException;
    Double consultarNota(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException;
    Double calcularMediaAluno(int alunoId) throws SQLException;
    Double calcularMediaDisciplina(int disciplinaId) throws SQLException;
}
