package org.isoron.uhabits.core.database

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

@Table(name = "tests")
class ThingRecord {
    @field:Column
    var id: Long? = null

    @field:Column
    var name: String? = null

    @field:Column(name = "color_number")
    var color: Int? = null

    @field:Column
    var score: Double? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val record = other as ThingRecord
        return EqualsBuilder()
            .append(id, record.id)
            .append(name, record.name)
            .append(color, record.color)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(name)
            .append(color)
            .toHashCode()
    }

    override fun toString(): String {
        return ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("color", color)
            .toString()
    }
}
