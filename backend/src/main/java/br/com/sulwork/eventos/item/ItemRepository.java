package br.com.sulwork.eventos.item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	boolean existsByEvento_Id(Long eventoId);

}
