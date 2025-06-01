package sdProject.dao;

import sdProject.models.CursoDisciplina;
import sdProject.models.Curso;
import sdProject.models.Disciplina;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class CursoDisciplinaDAO {
    private EntityManager em;
    
    public CursoDisciplinaDAO(EntityManager em) { 
        this.em = em;
    }

    public void salvar(CursoDisciplina cursoDisciplina) {
        try {
            em.getTransaction().begin();
            em.persist(cursoDisciplina);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public boolean removerRelacionamento(int cursoId, int disciplinaId) {
        try {
            em.getTransaction().begin();
            CursoDisciplina.CursoDisciplinaId id = new CursoDisciplina.CursoDisciplinaId(cursoId, disciplinaId);
            CursoDisciplina cursoDisciplina = em.find(CursoDisciplina.class, id);
            if (cursoDisciplina != null) {
                em.remove(cursoDisciplina);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public List<Disciplina> buscarDisciplinasPorCurso(int cursoId) {
        TypedQuery<Disciplina> query = em.createQuery(
            "SELECT cd.disciplina FROM CursoDisciplina cd WHERE cd.cursoId = :cursoId", 
            Disciplina.class);
        query.setParameter("cursoId", cursoId);
        return query.getResultList();
    }

    public List<Curso> buscarCursosPorDisciplina(int disciplinaId) {
        TypedQuery<Curso> query = em.createQuery(
            "SELECT cd.curso FROM CursoDisciplina cd WHERE cd.disciplinaId = :disciplinaId", 
            Curso.class);
        query.setParameter("disciplinaId", disciplinaId);
        return query.getResultList();
    }
}