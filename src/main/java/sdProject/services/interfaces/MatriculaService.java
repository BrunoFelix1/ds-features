package sdProject.services.interfaces;

import sdProject.models.Matricula;
import java.util.List;

public interface MatriculaService extends AutoCloseable {
    Matricula matricularAluno(int alunoId, int disciplinaId);
    boolean cancelarMatricula(int alunoId, int disciplinaId);
    List<Matricula> buscarMatriculasPorAluno(int alunoId);
    List<Matricula> buscarMatriculasPorDisciplina(int disciplinaId);
    boolean verificarMatricula(int alunoId, int disciplinaId);
}
