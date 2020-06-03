package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dbfiles")
@Builder
public class DBFile extends BaseEntity {
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fileName;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fileType;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    @JsonIgnore
    private byte[] data;
}
