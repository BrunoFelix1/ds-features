package sdProject.services.interfaces;

import sdProject.models.Matricula;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface que define os serviços relacionados às matrículas
 */
public interface MatriculaService {
    
    /**
     * Matricula um aluno em uma disciplina
     * @param alunoId ID do aluno
     * @param disciplinaId ID da disciplina
     * @return A matrícula criada
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     * @throws IllegalArgumentException Se o aluno já estiver matriculado na disciplina
     */
    Matricula matricularAluno(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException;
    
    /**
     * Cancela a matrícula de um aluno em uma disciplina
     * @param alunoId ID do aluno
     * @param disciplinaId ID da disciplina
     * @return true se a matrícula foi cancelada, false caso contrário
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    boolean cancelarMatricula(int alunoId, int disciplinaId) throws SQLException;
    
    /**
     * Busca todas as matrículas de um aluno
     * @param alunoId ID do aluno
     * @return Lista de matrículas do aluno
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    List<Matricula> buscarMatriculasPorAluno(int alunoId) throws SQLException;
    
    /**
     * Busca todas as matrículas em uma disciplina
     * @param disciplinaId ID da disciplina
     * @return Lista de matrículas na disciplina
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    List<Matricula> buscarMatriculasPorDisciplina(int disciplinaId) throws SQLException;
    
    /**
     * Verifica se um aluno está matriculado em uma disciplina
     * @param alunoId ID do aluno
     * @param disciplinaId ID da disciplina
     * @return true se o aluno estiver matriculado, false caso contrário
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    boolean verificarMatricula(int alunoId, int disciplinaId) throws SQLException;
}
