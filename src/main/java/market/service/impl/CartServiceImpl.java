package market.service.impl;

import market.dao.CartDAO;
import market.domain.Cart;
import market.domain.CartItem;
import market.domain.Product;
import market.domain.UserAccount;
import market.exception.UnknownEntityException;
import market.service.CartService;
import market.service.ProductService;
import market.service.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
	private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

	private final CartDAO cartDAO;
	private final UserAccountService userAccountService;
	private final ProductService productService;

	public CartServiceImpl(CartDAO cartDAO, UserAccountService userAccountService, ProductService productService) {
		this.cartDAO = cartDAO;
		this.userAccountService = userAccountService;
		this.productService = productService;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Cart getCartOrCreate(String userEmail) {
		UserAccount account = userAccountService.findByEmail(userEmail);
		Optional<Cart> cartOptional = cartDAO.findById(account.getId());
		return cartOptional.orElseGet(() -> createCart(account));
	}

	private Cart createCart(UserAccount account) {
		if (log.isDebugEnabled())
			log.debug("Creating new cart for account #" + account.getId());
		return cartDAO.save(new Cart(account));
	}

	@Transactional
	@Override
	public Cart addToCart(String userEmail, long productId, int quantity) throws UnknownEntityException {
		Cart cart = getCartOrCreate(userEmail);
		Product product = productService.getProduct(productId);
		if (product.isAvailable()) {
			cart.update(product, quantity);
			return cartDAO.save(cart);
		} else {
			return cart;
		}
	}

	@Transactional
	@Override
	public Cart addAllToCart(String userEmail, List<CartItem> itemsToAdd) {
		Cart cart = getCartOrCreate(userEmail);
		boolean updated = false;
		for (CartItem item : itemsToAdd) {
			Optional<Product> product = productService.findOne(item.getProduct().getId());
			if (product.isPresent() && product.get().isAvailable()) {
				cart.update(product.get(), item.getQuantity());
				updated = true;
			}
		}
		return updated ? cartDAO.save(cart) : cart;
	}

	@Transactional
	@Override
	public Cart setDelivery(String userEmail, boolean deliveryIncluded) {
		Cart cart = getCartOrCreate(userEmail);
		cart.setDeliveryIncluded(deliveryIncluded);
		return cartDAO.save(cart);
	}

	@Transactional
	@Override
	public Cart clearCart(String userEmail) {
		Cart cart = getCartOrCreate(userEmail);
		cart.clear();
		return cartDAO.save(cart);
	}
}
