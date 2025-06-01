package sdProject.models;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "curso_disciplina")
@IdClass(CursoDisciplina.CursoDisciplinaId.class)
public class CursoDisciplina {
    
    @Id
    @Column(name = "curso_id")
    private int cursoId;

    @Id
    @Column(name = "disciplina_id")
    private int disciplinaId;

    @ManyToOne
    @JoinColumn(name = "curso_id", insertable = false, updatable = false)
    private Curso curso;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", insertable = false, updatable = false)
    private Disciplina disciplina;

    public CursoDisciplina() {}

    public CursoDisciplina(int cursoId, int disciplinaId) {
        this.cursoId = cursoId;
        this.disciplinaId = disciplinaId;
    }

    public int getCursoId() { return cursoId; }
    public void setCursoId(int cursoId) { this.cursoId = cursoId; }

    public int getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(int disciplinaId) { this.disciplinaId = disciplinaId; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { 
        this.curso = curso;
        if (curso != null) this.cursoId = curso.getId();
    }

    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { 
        this.disciplina = disciplina;
        if (disciplina != null) this.disciplinaId = disciplina.getId();
    }

    public static class CursoDisciplinaId implements Serializable {
        private int cursoId;
        private int disciplinaId;

        public CursoDisciplinaId() {}

        public CursoDisciplinaId(int cursoId, int disciplinaId) {
            this.cursoId = cursoId;
            this.disciplinaId = disciplinaId;
        }

        public int getCursoId() { return cursoId; }
        public void setCursoId(int cursoId) { this.cursoId = cursoId; }

        public int getDisciplinaId() { return disciplinaId; }
        public void setDisciplinaId(int disciplinaId) { this.disciplinaId = disciplinaId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CursoDisciplinaId that = (CursoDisciplinaId) o;
            return cursoId == that.cursoId && disciplinaId == that.disciplinaId;
        }

        @Override
        public int hashCode() {
            return 31 * cursoId + disciplinaId;
        }
    }
}