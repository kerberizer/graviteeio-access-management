/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.repository.mongodb.management;

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Tag;
import io.gravitee.am.repository.management.api.TagRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.TagMongo;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoTagRepository extends AbstractManagementMongoRepository implements TagRepository {

    private static final String FIELD_ID = "_id";
    private MongoCollection<TagMongo> tagsCollection;

    @PostConstruct
    public void init() {
        tagsCollection = mongoOperations.getCollection("tags", TagMongo.class);
    }

    @Override
    public Maybe<Tag> findById(String id) {
        return Observable.fromPublisher(tagsCollection.find(eq(FIELD_ID, id)).first()).firstElement().map(this::convert);
    }

    @Override
    public Single<Set<Tag>> findAll() {
        return Observable.fromPublisher(tagsCollection.find()).map(this::convert).collect(HashSet::new, Set::add);
    }

    @Override
    public Single<Tag> create(Tag item) {
        TagMongo tag = convert(item);
        tag.setId(tag.getId() == null ? RandomString.generate() : tag.getId());
        return Single.fromPublisher(tagsCollection.insertOne(tag)).flatMap(success -> findById(tag.getId()).toSingle());
    }

    @Override
    public Single<Tag> update(Tag item) {
        TagMongo tag = convert(item);
        return Single.fromPublisher(tagsCollection.replaceOne(eq(FIELD_ID, tag.getId()), tag)).flatMap(updateResult -> findById(tag.getId()).toSingle());
    }

    @Override
    public Completable delete(String id) {
        return Completable.fromPublisher(tagsCollection.deleteOne(eq(FIELD_ID, id)));
    }

    private Tag convert(TagMongo tagMongo) {
        if (tagMongo == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setId(tagMongo.getId());
        tag.setName(tagMongo.getName());
        tag.setDescription(tagMongo.getDescription());
        tag.setCreatedAt(tagMongo.getCreatedAt());
        tag.setUpdatedAt(tagMongo.getUpdatedAt());

        return tag;
    }

    private TagMongo convert(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagMongo tagMongo = new TagMongo();
        tagMongo.setId(tag.getId());
        tagMongo.setName(tag.getName());
        tagMongo.setDescription(tag.getDescription());
        tagMongo.setCreatedAt(tag.getCreatedAt());
        tagMongo.setUpdatedAt(tag.getUpdatedAt());

        return tagMongo;
    }
}
