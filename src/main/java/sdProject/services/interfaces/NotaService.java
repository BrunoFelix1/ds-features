package sdProject.services.interfaces;

import java.sql.SQLException;

//Interface dos serviços de nota

public interface NotaService {
    boolean registrarNota(int alunoId, int disciplinaId, Double nota) throws SQLException, IllegalArgumentException;
    Double consultarNota(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException;
    Double calcularMediaAluno(int alunoId) throws SQLException;
    Double calcularMediaDisciplina(int disciplinaId) throws SQLException;
}
