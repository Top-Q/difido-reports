
package il.co.topq.report.business.elastic;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Groups",
    "Class",
    "index"
})
public class Properties {

    @JsonProperty("Groups")
    private String groups;
    @JsonProperty("Class")
    private String _class;
    @JsonProperty("index")
    private String index;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("Groups")
    public String getGroups() {
        return groups;
    }

    @JsonProperty("Groups")
    public void setGroups(String groups) {
        this.groups = groups;
    }

    @JsonProperty("Class")
    public String getClass_() {
        return _class;
    }

    @JsonProperty("Class")
    public void setClass_(String _class) {
        this._class = _class;
    }

    @JsonProperty("index")
    public String getIndex() {
        return index;
    }

    @JsonProperty("index")
    public void setIndex(String index) {
        this.index = index;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
