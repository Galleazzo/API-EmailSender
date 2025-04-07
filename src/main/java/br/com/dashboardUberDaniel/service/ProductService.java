```java

import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;```

package br.com.dashboardUberDaniel.service;

import br.com.dashboardUberDaniel.model.Product;
import br.com.dashboardUberDaniel.model.dto.ProductDTO;
import br.com.dashboardUberDaniel.repository.ProductRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${api_key_sendGrid}")
	private String apiToken;

	public ProductDTO save(ProductDTO productDTO) {
		Product product = new Product();
		product.setName(productDTO.getName());
		product.setPrice(productDTO.getPrice());
		product.setQuantity(productDTO.getQuantity());

		return modelMapper.map(this.productRepository.save(product), ProductDTO.class);
	}

	public ProductDTO update(Long id, ProductDTO productDTO) throws IOException {
		Optional<Product> productOp = this.productRepository.findById(id);
		if (productOp.isEmpty()) {
			throw new NoSuchElementException("Produto com ID " + id + " não encontrado.");
		}

		Product product = productOp.get();

		product.setName(productDTO.getName());
		product.setPrice(productDTO.getPrice());
		product.setQuantity(productDTO.getQuantity());

		if (product.getQuantity() == 0) {
			this.sendMessageToEmail(product);
		}

		Product savedProduct = this.productRepository.save(product);

		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Produto com ID {} atualizado com sucesso.", id);

		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	private void sendMessageToEmail(Product product) throws IOException {
		Email from = new Email("paulogalleazzo1@gmail.com");
		String subject = "ALERTA DE ITENS!!!!";
		Email to = new Email("danielgalleazzo@gmail.com");
		Content content = new Content("text/plain", "O item " + product.getName() + ", com o valor de R$"
				+ product.getPrice() + " está com os itens faltando! QUANTIDADE ATUAL: " + product.getQuantity());
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(this.apiToken);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
				System.out.println("Email enviado com sucesso!");
			} else {
				System.out.println("Falha ao enviar email. Código de status: " + response.getStatusCode());
				System.out.println(response.getBody());
			}
			System.out.println(response.getHeaders());
		} catch (IOException ex) {
			System.out.println("Erro ao enviar email: " + ex.getMessage());
			throw ex;
		}
	}
}
