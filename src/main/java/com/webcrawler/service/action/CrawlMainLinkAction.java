package com.webcrawler.service.action;

import com.webcrawler.model.ProductModel;
import com.webcrawler.service.util.JSoupUtil;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedList;

public class CrawlMainLinkAction extends RecursiveAction {
    private static final String LINK_ELEMENT_SELECTOR = "a[href]";
    private String mainUrl;
    Collection<CrawlCategoryLinkAction> crawlCategoryLinkActions;

    public CrawlMainLinkAction(String mainUrl) {
        this.mainUrl = mainUrl;
        crawlCategoryLinkActions = synchronizedList(new ArrayList());
    }

    @Override
    protected void compute() {
        doCrawlAction();
    }

    public void doCrawlAction() {
        Instant startTime = Instant.now();

        Elements linkElements = JSoupUtil.getElementFromUrl(mainUrl, LINK_ELEMENT_SELECTOR);

        if (linkElements != null && linkElements.size() > 0) {
            Set<String> urls = JSoupUtil.extractHtmlLink(linkElements);
            for (String link : urls) {
                crawlCategoryLinkActions.add(new CrawlCategoryLinkAction(link, mainUrl));
            }
            ForkJoinTask.invokeAll(crawlCategoryLinkActions);
        }
    }

    public Optional<List<ProductModel>> getProductList() {
        return Optional.of(crawlCategoryLinkActions
                .stream()
                .map(CrawlCategoryLinkAction::getProductList)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }
}