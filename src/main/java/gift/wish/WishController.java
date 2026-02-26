package gift.wish;

import gift.auth.LoginMember;
import gift.member.Member;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final WishService wishService;

    public WishController(
            WishRepository wishRepository,
            ProductRepository productRepository,
            AuthenticationResolver authenticationResolver) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
        this.authenticationResolver = authenticationResolver;
    }

    @GetMapping
    public ResponseEntity<Page<WishResponse>> getWishes(
            @RequestHeader("Authorization") String authorization, Pageable pageable) {
        // check auth
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        var wishes = wishRepository.findByMemberId(member.getId(), pageable).map(WishResponse::from);
        return ResponseEntity.ok(wishes);
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(
            @RequestHeader("Authorization") String authorization, @Valid @RequestBody WishRequest request) {
        // check auth
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }

        if (result.created()) {
            return ResponseEntity.created(
                            URI.create("/api/wishes/" + result.wish().getId()))
                    .body(response);
        }

        // check duplicate
        var existing = wishRepository
                .findByMemberIdAndProductId(member.getId(), product.getId())
                .orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(WishResponse.from(existing));
        }

        var saved = wishRepository.save(new Wish(member.getId(), product));
        return ResponseEntity.created(URI.create("/api/wishes/" + saved.getId()))
                .body(WishResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(
            @RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        // check auth
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }

        var wish = wishRepository.findById(id).orElse(null);
        if (wish == null) {
            return ResponseEntity.notFound().build();
        }

        if (!wish.getMemberId().equals(member.getId())) {
            return ResponseEntity.status(403).build();
        }

        wishRepository.delete(wish);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Void> handleForbidden(IllegalStateException e) {
        return ResponseEntity.status(403).build();
    }
}
