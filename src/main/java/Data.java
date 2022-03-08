import java.util.List;

public class Data {
    private List<String> attributesValues;
    private List<String> result;

    public Data(List<String> attributesValues, List<String> result) {
        this.attributesValues = attributesValues;
        this.result = result;
    }

    public List<String> getAttributesValues() {
        return attributesValues;
    }

    public List<String> getResult() {
        return result;
    }

}
