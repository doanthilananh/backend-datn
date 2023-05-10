package com.bookshop.dao;

import com.bookshop.helpers.StringHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(nullable = false)
    private String title;

    @Nationalized
    private String shortDescription;

    @Nationalized
    @Column(nullable = false)
    @Length(max = 100000)
    private String longDescription;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ProductImage> productImages;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ProductRate> productRates;

    @Column(nullable = false)
    private Long price;

    @Nationalized
    private String author;

    @Column(nullable = false)
    private Integer currentNumber;

    @Column(nullable = false)
    private Integer numberOfPage;

    @Column(nullable = false)
    private String slug;

    private Integer quantityPurchased;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    private Set<SizeProduct> sizeProducts;
    @PrePersist
    public void PrePersist() {
        if (this.quantityPurchased == null) {
            this.quantityPurchased = 0;
        }
        this.slug = StringHelper.toSlug(this.title);
        this.shortDescription = this.longDescription.length() >= 60
                ? this.longDescription.substring(0, 60)
                : this.longDescription;
    }

    @PreUpdate
    public void PreUpdate() {
        this.slug = StringHelper.toSlug(this.title);
        this.shortDescription = this.longDescription.length() >= 60
                ? this.longDescription.substring(0, 60)
                : this.longDescription;
    }
}
