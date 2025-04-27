package sdProject.services;

import sdProject.models.Disciplina;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;

public interface HistoricoService {
    Map<Disciplina, Double> gerarHistoricoCompleto(int alunoId) throws SQLException, IllegalArgumentException;
    Map<Disciplina, Double> listarDisciplinasAprovadas(int alunoId) throws SQLException;
    Map<Disciplina, Double> listarDisciplinasReprovadas(int alunoId) throws SQLException;
    List<Disciplina> listarDisciplinasEmCurso(int alunoId) throws SQLException;
    Double calcularCoeficienteRendimento(int alunoId) throws SQLException;
}
