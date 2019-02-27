package me.juhezi.eternal.model.bean;

import java.util.List;

import me.juhezi.eternal.model.Article;

/**
 * Created by Juhezi[juhezix@163.com] on 2018/9/5.
 */
public class ArticleListWrapper {

    private List<Article> articles;

    public List<Article> getArticles() {
        return articles;
    }

    public ArticleListWrapper setArticles(List<Article> articles) {
        this.articles = articles;
        return this;
    }
}
