import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class RatioCalculator {

    private final List<Data> dataset;
    private final List<String> attributesNames;
    private final Map<Integer, List<String>> attributesValues;

    public RatioCalculator(List<String> attributesNames, List<Data> dataset) {
        if (dataset == null || dataset.size() == 0) {
            throw new IllegalArgumentException("Dataset is not valid");
        }

        this.dataset = dataset;
        this.attributesNames = attributesNames;
        this.attributesValues = getAttributesValues();
    }

    public static List<Data> buildDataset(List<String> headerNames, List<CSVRecord> records, int attributesCount) {
        return records.stream()
                .map(r -> {
                    List<String> attributeValues = new ArrayList<>();
                    for (int i = 0; i < attributesCount; i++) {
                        attributeValues.add(r.get(i));
                    }

                    List<String> label = new ArrayList<>();
                    for (int i = attributesCount; i < headerNames.size(); i++) {
                        label.add(r.get(i));
                    }

                    return new Data(attributeValues, label);
                })
                .collect(Collectors.toList());
    }

    public static double log2(float n) {
        return (Math.log(n) / Math.log(2));
    }

    private Map<Integer, List<String>> getAttributesValues() {
        Map<Integer, Set<String>> uniqueAttributesSets = new HashMap<>();

        for (Data data : dataset) {
            List<String> attributesValues = data.getAttributesValues();
            for (int i = 0; i < attributesValues.size(); i++) {
                String attributesValue = attributesValues.get(i);
                if (attributesValue != null && attributesValue.length() > 0) {
                    uniqueAttributesSets.computeIfAbsent(i, key -> new HashSet<>()).add(attributesValue);
                }
            }
        }

        return uniqueAttributesSets.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, uniqueSet -> new ArrayList<>(uniqueSet.getValue()), (a, b) -> b));
    }

    public Map<String, Float> calcGainRatio() {
        Map<String, Float> result = new HashMap<>();

        for (int i = 0; i < attributesNames.size(); i++) {
            List<Data> filteredDataset = preprocessDatasetForAttribute(dataset, i);
            float value = gain(filteredDataset, i) / intrinsicInfo(filteredDataset, i);
            result.put(attributesNames.get(i), value);
        }

        return result;
    }

    private List<Data> preprocessDatasetForAttribute(List<Data> dataSet, int i) {
        return dataSet.stream()
                .filter(data -> data != null && data.getAttributesValues().get(i).length() > 0)
                .collect(Collectors.toList());
    }

    private float gain(List<Data> dataSet, int attributesNumber) {
        return entropy(dataSet) - averageEntropy(dataSet, attributesNumber);
    }

    private float averageEntropy(List<Data> dataSet, int attributesNumber) {
        int dataSetSize = dataSet.size();
        Map<String, List<Data>> dataSubsets = divideSet(dataSet, attributesNumber);

        float value = 0;
        for (Map.Entry<String, List<Data>> entry : dataSubsets.entrySet()) {
            value += (entry.getValue().size() / (float) dataSetSize) * entropy(entry.getValue());
        }

        return value;
    }

    private float entropy(List<Data> dataSet) {
        Map<List<String>, Integer> resultCounts = calcLabelsCount(dataSet);
        return calcLogSum(resultCounts, dataSet.size());
    }

    private float intrinsicInfo(List<Data> dataSet, int attributesNumber) {
        HashMap<String, Integer> dataSubsetCounts = new HashMap<>();

        for (String attributeValue : attributesValues.get(attributesNumber)) {
            dataSubsetCounts.put(attributeValue, 0);
        }

        for (Data data : dataSet) {
            String attributeValue = data.getAttributesValues().get(attributesNumber);
            int dataSubsetCount = dataSubsetCounts.get(attributeValue);
            dataSubsetCounts.put(attributeValue, ++dataSubsetCount);
        }

        return calcLogSum(dataSubsetCounts, dataSet.size());
    }

    private float calcLogSum(Map<?, Integer> counts, int dataSetSize) {
        float value = 0;

        for (Map.Entry<?, Integer> entry : counts.entrySet()) {
            float p = entry.getValue() / (float) dataSetSize;
            value += p * log2(p);
        }

        return (-1) * value;
    }

    private HashMap<List<String>, Integer> calcLabelsCount(List<Data> dataSet) {
        HashMap<List<String>, Integer> resultCounts = new HashMap<>();

        for (Data data : dataSet) {
            int count = resultCounts.getOrDefault(data.getResult(), 0);
            resultCounts.put(data.getResult(), ++count);
        }

        return resultCounts;
    }

    private Map<String, List<Data>> divideSet(List<Data> dataSet, int attribute) {
        Map<String, List<Data>> dataSubsets = new HashMap<>();

        for (String attributeValue : attributesValues.get(attribute)) {
            dataSubsets.put(attributeValue, new ArrayList<>());
        }

        for (Data data : dataSet) {
            dataSubsets.get(data.getAttributesValues().get(attribute)).add(data);
        }

        return dataSubsets;
    }

}
