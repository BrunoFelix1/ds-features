package sdProject.services.interfaces;

import sdProject.models.Matricula;
import java.sql.SQLException;
import java.util.List;

public interface MatriculaService {
    Matricula matricularAluno(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException;
    boolean cancelarMatricula(int alunoId, int disciplinaId) throws SQLException;
    List<Matricula> buscarMatriculasPorAluno(int alunoId) throws SQLException;
    List<Matricula> buscarMatriculasPorDisciplina(int disciplinaId) throws SQLException;
    boolean verificarMatricula(int alunoId, int disciplinaId) throws SQLException;
}
