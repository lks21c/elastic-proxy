package com.creamsugardonut.kibanaproxy.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ParseFieldRegistry;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.InternalAdjacencyMatrix;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.InternalComposite;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilters;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.InternalGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.InternalGlobal;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.missing.InternalMissing;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.InternalReverseNested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.InternalBinaryRange;
import org.elasticsearch.search.aggregations.bucket.range.InternalDateRange;
import org.elasticsearch.search.aggregations.bucket.range.InternalGeoDistance;
import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.InternalSampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.UnmappedSampler;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantLongTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTextAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.UnmappedSignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.SignificanceHeuristicParser;
import org.elasticsearch.search.aggregations.bucket.terms.DoubleTerms;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geobounds.InternalGeoBounds;
import org.elasticsearch.search.aggregations.metrics.geocentroid.GeoCentroidAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geocentroid.InternalGeoCentroid;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.InternalMin;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.hdr.InternalHDRPercentileRanks;
import org.elasticsearch.search.aggregations.metrics.percentiles.hdr.InternalHDRPercentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.tdigest.InternalTDigestPercentileRanks;
import org.elasticsearch.search.aggregations.metrics.percentiles.tdigest.InternalTDigestPercentiles;
import org.elasticsearch.search.aggregations.metrics.scripted.InternalScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetricAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.InternalExtendedStats;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.InternalTopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.InternalValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParsingService {
    private static final Logger logger = LogManager.getLogger(ParsingService.class);

    private final List<NamedWriteableRegistry.Entry> namedWriteables = new ArrayList<>();
    private final List<NamedXContentRegistry.Entry> namedXContents = new ArrayList<>();

    private final ParseFieldRegistry<SignificanceHeuristicParser> significanceHeuristicParserRegistry = new ParseFieldRegistry<>(
            "significance_heuristic");

    public Map<String,Object> parseXContent(String str){
        return XContentHelper.convertToMap(XContentType.JSON.xContent(), str, true);
    }

    public AggregatorFactories.Builder parseAggs(String aggs) throws IOException {
        registerAggregation(new SearchPlugin.AggregationSpec(AvgAggregationBuilder.NAME, AvgAggregationBuilder::new, AvgAggregationBuilder::parse)
                .addResultReader(InternalAvg::new));
        registerAggregation(new SearchPlugin.AggregationSpec(SumAggregationBuilder.NAME, SumAggregationBuilder::new, SumAggregationBuilder::parse)
                .addResultReader(InternalSum::new));
        registerAggregation(new SearchPlugin.AggregationSpec(MinAggregationBuilder.NAME, MinAggregationBuilder::new, MinAggregationBuilder::parse)
                .addResultReader(InternalMin::new));
        registerAggregation(new SearchPlugin.AggregationSpec(MaxAggregationBuilder.NAME, MaxAggregationBuilder::new, MaxAggregationBuilder::parse)
                .addResultReader(InternalMax::new));
        registerAggregation(new SearchPlugin.AggregationSpec(StatsAggregationBuilder.NAME, StatsAggregationBuilder::new, StatsAggregationBuilder::parse)
                .addResultReader(InternalStats::new));
        registerAggregation(new SearchPlugin.AggregationSpec(ExtendedStatsAggregationBuilder.NAME, ExtendedStatsAggregationBuilder::new,
                ExtendedStatsAggregationBuilder::parse).addResultReader(InternalExtendedStats::new));
        registerAggregation(new SearchPlugin.AggregationSpec(ValueCountAggregationBuilder.NAME, ValueCountAggregationBuilder::new,
                ValueCountAggregationBuilder::parse).addResultReader(InternalValueCount::new));
        registerAggregation(new SearchPlugin.AggregationSpec(PercentilesAggregationBuilder.NAME, PercentilesAggregationBuilder::new,
                PercentilesAggregationBuilder::parse)
                .addResultReader(InternalTDigestPercentiles.NAME, InternalTDigestPercentiles::new)
                .addResultReader(InternalHDRPercentiles.NAME, InternalHDRPercentiles::new));
        registerAggregation(new SearchPlugin.AggregationSpec(PercentileRanksAggregationBuilder.NAME, PercentileRanksAggregationBuilder::new,
                PercentileRanksAggregationBuilder::parse)
                .addResultReader(InternalTDigestPercentileRanks.NAME, InternalTDigestPercentileRanks::new)
                .addResultReader(InternalHDRPercentileRanks.NAME, InternalHDRPercentileRanks::new));
        registerAggregation(new SearchPlugin.AggregationSpec(CardinalityAggregationBuilder.NAME, CardinalityAggregationBuilder::new,
                CardinalityAggregationBuilder::parse).addResultReader(InternalCardinality::new));
        registerAggregation(new SearchPlugin.AggregationSpec(GlobalAggregationBuilder.NAME, GlobalAggregationBuilder::new,
                GlobalAggregationBuilder::parse).addResultReader(InternalGlobal::new));
        registerAggregation(new SearchPlugin.AggregationSpec(MissingAggregationBuilder.NAME, MissingAggregationBuilder::new,
                MissingAggregationBuilder::parse).addResultReader(InternalMissing::new));
        registerAggregation(new SearchPlugin.AggregationSpec(FilterAggregationBuilder.NAME, FilterAggregationBuilder::new,
                FilterAggregationBuilder::parse).addResultReader(InternalFilter::new));
        registerAggregation(new SearchPlugin.AggregationSpec(FiltersAggregationBuilder.NAME, FiltersAggregationBuilder::new,
                FiltersAggregationBuilder::parse).addResultReader(InternalFilters::new));
        registerAggregation(new SearchPlugin.AggregationSpec(AdjacencyMatrixAggregationBuilder.NAME, AdjacencyMatrixAggregationBuilder::new,
                AdjacencyMatrixAggregationBuilder::parse).addResultReader(InternalAdjacencyMatrix::new));
        registerAggregation(new SearchPlugin.AggregationSpec(SamplerAggregationBuilder.NAME, SamplerAggregationBuilder::new,
                SamplerAggregationBuilder::parse)
                .addResultReader(InternalSampler.NAME, InternalSampler::new)
                .addResultReader(UnmappedSampler.NAME, UnmappedSampler::new));
        registerAggregation(new SearchPlugin.AggregationSpec(DiversifiedAggregationBuilder.NAME, DiversifiedAggregationBuilder::new,
                        DiversifiedAggregationBuilder::parse)
                /* Reuses result readers from SamplerAggregator*/);
        registerAggregation(new SearchPlugin.AggregationSpec(TermsAggregationBuilder.NAME, TermsAggregationBuilder::new,
                TermsAggregationBuilder::parse)
                .addResultReader(StringTerms.NAME, StringTerms::new)
                .addResultReader(UnmappedTerms.NAME, UnmappedTerms::new)
                .addResultReader(LongTerms.NAME, LongTerms::new)
                .addResultReader(DoubleTerms.NAME, DoubleTerms::new));
        registerAggregation(new SearchPlugin.AggregationSpec(SignificantTermsAggregationBuilder.NAME, SignificantTermsAggregationBuilder::new,
                SignificantTermsAggregationBuilder.getParser(significanceHeuristicParserRegistry))
                .addResultReader(SignificantStringTerms.NAME, SignificantStringTerms::new)
                .addResultReader(SignificantLongTerms.NAME, SignificantLongTerms::new)
                .addResultReader(UnmappedSignificantTerms.NAME, UnmappedSignificantTerms::new));
        registerAggregation(new SearchPlugin.AggregationSpec(SignificantTextAggregationBuilder.NAME, SignificantTextAggregationBuilder::new,
                SignificantTextAggregationBuilder.getParser(significanceHeuristicParserRegistry)));
        registerAggregation(new SearchPlugin.AggregationSpec(RangeAggregationBuilder.NAME, RangeAggregationBuilder::new,
                RangeAggregationBuilder::parse).addResultReader(InternalRange::new));
        registerAggregation(new SearchPlugin.AggregationSpec(DateRangeAggregationBuilder.NAME, DateRangeAggregationBuilder::new,
                DateRangeAggregationBuilder::parse).addResultReader(InternalDateRange::new));
        registerAggregation(new SearchPlugin.AggregationSpec(IpRangeAggregationBuilder.NAME, IpRangeAggregationBuilder::new,
                IpRangeAggregationBuilder::parse).addResultReader(InternalBinaryRange::new));
        registerAggregation(new SearchPlugin.AggregationSpec(HistogramAggregationBuilder.NAME, HistogramAggregationBuilder::new,
                HistogramAggregationBuilder::parse).addResultReader(InternalHistogram::new));
        registerAggregation(new SearchPlugin.AggregationSpec(DateHistogramAggregationBuilder.NAME, DateHistogramAggregationBuilder::new,
                DateHistogramAggregationBuilder::parse).addResultReader(InternalDateHistogram::new));
        registerAggregation(new SearchPlugin.AggregationSpec(GeoDistanceAggregationBuilder.NAME, GeoDistanceAggregationBuilder::new,
                GeoDistanceAggregationBuilder::parse).addResultReader(InternalGeoDistance::new));
        registerAggregation(new SearchPlugin.AggregationSpec(GeoGridAggregationBuilder.NAME, GeoGridAggregationBuilder::new,
                GeoGridAggregationBuilder::parse).addResultReader(InternalGeoHashGrid::new));
        registerAggregation(new SearchPlugin.AggregationSpec(NestedAggregationBuilder.NAME, NestedAggregationBuilder::new,
                NestedAggregationBuilder::parse).addResultReader(InternalNested::new));
        registerAggregation(new SearchPlugin.AggregationSpec(ReverseNestedAggregationBuilder.NAME, ReverseNestedAggregationBuilder::new,
                ReverseNestedAggregationBuilder::parse).addResultReader(InternalReverseNested::new));
        registerAggregation(new SearchPlugin.AggregationSpec(TopHitsAggregationBuilder.NAME, TopHitsAggregationBuilder::new,
                TopHitsAggregationBuilder::parse).addResultReader(InternalTopHits::new));
        registerAggregation(new SearchPlugin.AggregationSpec(GeoBoundsAggregationBuilder.NAME, GeoBoundsAggregationBuilder::new,
                GeoBoundsAggregationBuilder::parse).addResultReader(InternalGeoBounds::new));
        registerAggregation(new SearchPlugin.AggregationSpec(GeoCentroidAggregationBuilder.NAME, GeoCentroidAggregationBuilder::new,
                GeoCentroidAggregationBuilder::parse).addResultReader(InternalGeoCentroid::new));
        registerAggregation(new SearchPlugin.AggregationSpec(ScriptedMetricAggregationBuilder.NAME, ScriptedMetricAggregationBuilder::new,
                ScriptedMetricAggregationBuilder::parse).addResultReader(InternalScriptedMetric::new));
        registerAggregation((new SearchPlugin.AggregationSpec(CompositeAggregationBuilder.NAME, CompositeAggregationBuilder::new,
                CompositeAggregationBuilder::parse).addResultReader(InternalComposite::new)));
        NamedXContentRegistry registry = new NamedXContentRegistry(namedXContents);

        XContentParser parser = JsonXContent.jsonXContent.createParser(registry, aggs);
        parser.nextToken();

        AggregatorFactories.Builder ab = AggregatorFactories.parseAggregators(parser);
        return ab;
    }

    private void registerAggregation(SearchPlugin.AggregationSpec spec) {
        if (false == false) {
            namedXContents.add(new NamedXContentRegistry.Entry(BaseAggregationBuilder.class, spec.getName(), (p, c) -> {
                AggregatorFactories.AggParseContext context = (AggregatorFactories.AggParseContext) c;
                return spec.getParser().parse(context.name, p);
            }));
        }
        namedWriteables.add(
                new NamedWriteableRegistry.Entry(AggregationBuilder.class, spec.getName().getPreferredName(), spec.getReader()));
        for (Map.Entry<String, Writeable.Reader<? extends InternalAggregation>> t : spec.getResultReaders().entrySet()) {
            String writeableName = t.getKey();
            Writeable.Reader<? extends InternalAggregation> internalReader = t.getValue();
            namedWriteables.add(new NamedWriteableRegistry.Entry(InternalAggregation.class, writeableName, internalReader));
        }
    }
}
