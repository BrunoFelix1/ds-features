package sdProject.controllers;

import sdProject.services.interfaces.NotaService;
import sdProject.services.impl.NotaServiceImpl;
import sdProject.dto.NotaDTO;
import sdProject.dto.ResponseDTO;

public class NotaController implements AutoCloseable {
    
    private final NotaService notaService;
    
    public NotaController() {
        this.notaService = new NotaServiceImpl();
    }
    
    public ResponseDTO<Boolean> registrarNota(int alunoId, int disciplinaId, Double nota) {
        try {
            boolean registrada = notaService.registrarNota(alunoId, disciplinaId, nota);
            if (registrada) {
                return ResponseDTO.success("Nota registrada com sucesso", true);
            } else {
                return ResponseDTO.error("Não foi possível registrar a nota");
            }
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao registrar nota: " + e.getMessage());
        }
    }
    
    public ResponseDTO<NotaDTO> consultarNota(int alunoId, int disciplinaId) {
        try {
            Double nota = notaService.consultarNota(alunoId, disciplinaId);
            NotaDTO dto = new NotaDTO();
            dto.setAlunoId(alunoId);
            dto.setDisciplinaId(disciplinaId);
            dto.setNota(nota);
            return ResponseDTO.success(dto);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao consultar nota: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Double> calcularMediaAluno(int alunoId) {
        try {
            Double media = notaService.calcularMediaAluno(alunoId);
            return ResponseDTO.success(media);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao calcular média do aluno: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Double> calcularMediaDisciplina(int disciplinaId) {
        try {
            Double media = notaService.calcularMediaDisciplina(disciplinaId);
            return ResponseDTO.success(media);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao calcular média da disciplina: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        notaService.close();
    }
}
