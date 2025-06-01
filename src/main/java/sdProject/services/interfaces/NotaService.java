package sdProject.services.interfaces;

public interface NotaService extends AutoCloseable {
    boolean registrarNota(int alunoId, int disciplinaId, Double nota);
    Double consultarNota(int alunoId, int disciplinaId);
    Double calcularMediaAluno(int alunoId);
    Double calcularMediaDisciplina(int disciplinaId);
}
