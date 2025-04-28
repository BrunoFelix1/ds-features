package sdProject.models;

public class CursoDisciplina {
    private int cursoId;
    private int disciplinaId;

    public CursoDisciplina() {}

    public CursoDisciplina(int cursoId, int disciplinaId) {
        this.cursoId = cursoId;
        this.disciplinaId = disciplinaId;
    }

    public int getCursoId() { return cursoId; }
    public void setCursoId(int cursoId) { this.cursoId = cursoId; }

    public int getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(int disciplinaId) { this.disciplinaId = disciplinaId; }
}