package br.com.dashboardUberDaniel.controller;

import br.com.dashboardUberDaniel.model.Product;
import br.com.dashboardUberDaniel.model.dto.ProductDTO;
import br.com.dashboardUberDaniel.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private ProductService productService;

	@PostMapping
	public ProductDTO save(@RequestBody ProductDTO productDTO) {
		return this.productService.save(productDTO);
	}

	@PatchMapping("/{id}")
	public ProductDTO update(@PathVariable Long id, @RequestBody ProductDTO productDTO) throws IOException {
		ProductDTO updatedProduct = this.productService.update(id, productDTO);
		System.out.println("Produto com ID " + id + " atualizado com sucesso.");
		return updatedProduct;
	}
}
