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
        Product product = productOp.get();

        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());

        if (product.getQuantity() == 0)
            this.sendMessageToEmail(product);

        return modelMapper.map(this.productRepository.save(product), ProductDTO.class);
    }

    private void sendMessageToEmail(Product product) throws IOException {
        Email from = new Email("paulogalleazzo1@gmail.com");
        String subject = "ALERTA DE ITENS!!!!";
        Email to = new Email("danielgalleazzo@gmail.com");
        Content content = new Content("text/plain", "O item " + product.getName() + ", com o valor de R$" + product.getPrice() + " esta com os itens faltando! QUANTIDADE ATUAL: " + product.getQuantity());
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(this.apiToken);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
