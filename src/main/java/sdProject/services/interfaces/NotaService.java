package sdProject.services.interfaces;

import java.sql.SQLException;

/**
 * Interface que define os serviços relacionados às notas dos alunos
 */
public interface NotaService {
    
    /**
     * Registra ou atualiza a nota de um aluno em uma disciplina
     * @param alunoId ID do aluno
     * @param disciplinaId ID da disciplina
     * @param nota Nota a ser registrada
     * @return true se a nota foi registrada com sucesso, false caso contrário
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     * @throws IllegalArgumentException Se o aluno não estiver matriculado na disciplina ou se a nota for inválida
     */
    boolean registrarNota(int alunoId, int disciplinaId, Double nota) throws SQLException, IllegalArgumentException;
    
    /**
     * Consulta a nota de um aluno em uma disciplina
     * @param alunoId ID do aluno
     * @param disciplinaId ID da disciplina
     * @return A nota do aluno na disciplina, ou null se não houver nota registrada
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     * @throws IllegalArgumentException Se o aluno não estiver matriculado na disciplina
     */
    Double consultarNota(int alunoId, int disciplinaId) throws SQLException, IllegalArgumentException;
    
    /**
     * Calcula a média das notas de um aluno em todas as disciplinas
     * @param alunoId ID do aluno
     * @return A média das notas do aluno
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    Double calcularMediaAluno(int alunoId) throws SQLException;
    
    /**
     * Calcula a média das notas de todos os alunos em uma disciplina
     * @param disciplinaId ID da disciplina
     * @return A média das notas na disciplina
     * @throws SQLException Em caso de erro no acesso ao banco de dados
     */
    Double calcularMediaDisciplina(int disciplinaId) throws SQLException;
}
