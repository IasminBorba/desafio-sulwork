package br.com.sulwork.eventos.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlergenoRepository extends JpaRepository<Alergeno, Long> {

	List<Alergeno> findAllByOrderByNomeAsc();

}
