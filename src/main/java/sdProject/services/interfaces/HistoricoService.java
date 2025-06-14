package sdProject.services.interfaces;

import sdProject.models.Disciplina;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;

//Interface dos serviços de histórico

public interface HistoricoService {
    Map<Disciplina, Double> gerarHistoricoCompleto(int alunoId) throws SQLException, IllegalArgumentException;
    Map<Disciplina, Double> listarDisciplinasAprovadas(int alunoId) throws SQLException;
    Map<Disciplina, Double> listarDisciplinasReprovadas(int alunoId) throws SQLException;
    List<Disciplina> listarDisciplinasEmCurso(int alunoId) throws SQLException;
}
