package sdProject.dao;

import sdProject.models.Aluno;
import java.sql.*;
import java.util.*;

public class AlunoDAO {
    private Connection conn;
    public AlunoDAO(Connection conn) { this.conn = conn; }

    public void salvar(Aluno aluno) throws SQLException {
        String sql = "INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getMatricula());
            stmt.setBoolean(4, aluno.isAtivo());
            stmt.setInt(5, aluno.getCursoId());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    aluno.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Aluno findById(int id) throws SQLException {
        String sql = "SELECT * FROM aluno WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Aluno aluno = new Aluno();
                    aluno.setId(rs.getInt("id"));
                    aluno.setNome(rs.getString("nome"));
                    aluno.setCpf(rs.getString("cpf"));
                    aluno.setMatricula(rs.getString("matricula"));
                    aluno.setAtivo(rs.getBoolean("ativo"));
                    aluno.setCursoId(rs.getInt("curso_id"));
                    return aluno;
                }
            }
        }
        return null;
    }

    public Aluno findByCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM aluno WHERE cpf = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Aluno aluno = new Aluno();
                    aluno.setId(rs.getInt("id"));
                    aluno.setNome(rs.getString("nome"));
                    aluno.setCpf(rs.getString("cpf"));
                    aluno.setMatricula(rs.getString("matricula"));
                    aluno.setAtivo(rs.getBoolean("ativo"));
                    aluno.setCursoId(rs.getInt("curso_id"));
                    return aluno;
                }
            }
        }
        return null;
    }
    
    public Aluno findByMatricula(String matricula) throws SQLException {
        String sql = "SELECT * FROM aluno WHERE matricula = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Aluno aluno = new Aluno();
                    aluno.setId(rs.getInt("id"));
                    aluno.setNome(rs.getString("nome"));
                    aluno.setCpf(rs.getString("cpf"));
                    aluno.setMatricula(rs.getString("matricula"));
                    aluno.setAtivo(rs.getBoolean("ativo"));
                    aluno.setCursoId(rs.getInt("curso_id"));
                    return aluno;
                }
            }
        }
        return null;
    }
    
    public List<Aluno> listarTodos() throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT * FROM aluno ORDER BY nome";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Aluno aluno = new Aluno();
                aluno.setId(rs.getInt("id"));
                aluno.setNome(rs.getString("nome"));
                aluno.setCpf(rs.getString("cpf"));
                aluno.setMatricula(rs.getString("matricula"));
                aluno.setAtivo(rs.getBoolean("ativo"));
                aluno.setCursoId(rs.getInt("curso_id"));
                alunos.add(aluno);
            }
        }
        return alunos;
    }
    
    public List<Aluno> findByCurso(int cursoId) throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String sql = "SELECT * FROM aluno WHERE curso_id = ? ORDER BY nome";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Aluno aluno = new Aluno();
                    aluno.setId(rs.getInt("id"));
                    aluno.setNome(rs.getString("nome"));
                    aluno.setCpf(rs.getString("cpf"));
                    aluno.setMatricula(rs.getString("matricula"));
                    aluno.setAtivo(rs.getBoolean("ativo"));
                    aluno.setCursoId(rs.getInt("curso_id"));
                    alunos.add(aluno);
                }
            }
        }
        return alunos;
    }
    
    public boolean atualizar(Aluno aluno) throws SQLException {
        String sql = "UPDATE aluno SET nome = ?, cpf = ?, matricula = ?, ativo = ?, curso_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, aluno.getNome());
            stmt.setString(2, aluno.getCpf());
            stmt.setString(3, aluno.getMatricula());
            stmt.setBoolean(4, aluno.isAtivo());
            stmt.setInt(5, aluno.getCursoId());
            stmt.setInt(6, aluno.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM aluno WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
