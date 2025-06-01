package sdProject.services.interfaces;

import sdProject.models.Disciplina;
import java.util.List;
import java.util.Map;

public interface HistoricoService extends AutoCloseable {
    Map<Disciplina, Double> gerarHistoricoCompleto(int alunoId);
    Map<Disciplina, Double> listarDisciplinasAprovadas(int alunoId);
    Map<Disciplina, Double> listarDisciplinasReprovadas(int alunoId);
    List<Disciplina> listarDisciplinasEmCurso(int alunoId);
    Double calcularCoeficienteRendimento(int alunoId);
}
