package sdProject.dao;

import sdProject.models.Aluno;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class AlunoDAO {
    private EntityManager em;

    public AlunoDAO(EntityManager em) {
        this.em = em;
    }

    public void salvar(Aluno aluno) {
        try {
            em.getTransaction().begin();
            em.persist(aluno);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public Aluno findById(int id) {
        return em.find(Aluno.class, id);
    }

    public Aluno findByCpf(String cpf) {
        TypedQuery<Aluno> query = em.createQuery(
            "SELECT a FROM Aluno a WHERE a.cpf = :cpf", Aluno.class);
        query.setParameter("cpf", cpf);
        List<Aluno> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public Aluno findByMatricula(String matricula) {
        TypedQuery<Aluno> query = em.createQuery(
            "SELECT a FROM Aluno a WHERE a.matricula = :matricula", Aluno.class);
        query.setParameter("matricula", matricula);
        List<Aluno> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    public List<Aluno> listarTodos() {
        return em.createQuery("SELECT a FROM Aluno a ORDER BY a.nome", Aluno.class)
                 .getResultList();
    }
    
    public List<Aluno> findByCurso(int cursoId) {
        TypedQuery<Aluno> query = em.createQuery(
            "SELECT a FROM Aluno a WHERE a.cursoId = :cursoId ORDER BY a.nome", Aluno.class);
        query.setParameter("cursoId", cursoId);
        return query.getResultList();
    }
    
    public boolean atualizar(Aluno aluno) {
        try {
            em.getTransaction().begin();
            em.merge(aluno);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public boolean delete(int id) {
        try {
            em.getTransaction().begin();
            Aluno aluno = em.find(Aluno.class, id);
            if (aluno != null) {
                em.remove(aluno);
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
}
