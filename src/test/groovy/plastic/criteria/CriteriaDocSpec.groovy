package plastic.criteria

import grails.test.hibernate.HibernateSpec
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification

import static plastic.criteria.PlasticCriteria.mockCriteria

@Mock([Artist, Portrait, City])
@TestFor(PlasticCriteria)
class CriteriaDocSpec extends HibernateSpec{
    List<Class> getDomainClasses() { [Artist, Portrait, City] }

    def setupSpec(){
        mockCriteria([Artist, Portrait])
    }

    def setup() {
    }

    def cleanup() {
    }

    //projection tests
    def "test Group Property"(){
        given:
        def pablo = new Artist(name: 'Pablo').save()
        def salvador = new Artist(name: 'Salvador').save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon", value: 10.00).save()
        new Portrait(artist: pablo, name: "Les Noces de Pierrette", value: 22.00, color: 'blue').save()
        new Portrait(artist: salvador, name: "The Persistence of Memory", value: 20.00).save()

        when:
        def artistValue = Portrait.withCriteria {
            projections {
                sum('value')
                groupProperty('artist')
            }
        }

        then:
        artistValue ==  [[32.00, pablo], [20.00, salvador]]
    }

    def "test 2x Group Property"() {
        given:
        def pablo = new Artist(name: 'Pablo').save()
        def salvador = new Artist(name: 'Salvador').save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon 1", value: 10.00).save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon 2", value: 10.00).save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon 3", value: 10.00).save()
        new Portrait(artist: salvador, name: "The Persistence of Memory 1", value: 20.00).save()
        new Portrait(artist: salvador, name: "The Persistence of Memory 2", value: 20.00).save()
        new Portrait(artist: salvador, name: "The Persistence of Memory 3", value: 20.00).save()

        when:
        def artistValue = Portrait.withCriteria {
            projections {
                groupProperty('value')
                groupProperty('artist')
            }
        }

        then:
        artistValue.toSet() == [[10.00, pablo], [20.00, salvador]].toSet()
    }

    def "test And"(){
        given:
        def pablo = new Artist(name: 'Pablo').save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon", value: 10.00, color: 'orange').save()
        new Portrait(artist: pablo, name: "Les Noces de Pierrette", value: 22.00, color: 'blue').save()

        when:
        def portraits = Portrait.withCriteria {
            artist {
                eq('name', 'Pablo')
            }
            eq('color', 'blue')
        }
        then:
        portraits.size() == 1
        portraits[0].name == "Les Noces de Pierrette"
    }

    def "test Or"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def plastic1 = new Portrait(artist: artitst, name: 'Soleil levant').save()
        def plastic2 = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat').save()
        def plastic3 = new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon").save()

        when:
        def ls = Portrait.withCriteria {
            or {
                eq('name', 'Soleil levant')
                eq('name', 'The Madonna of Port Lligat')
            }
        }

        then:
        [plastic1, plastic2] == ls
    }

    def "test Avg"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def average = Portrait.createCriteria().get {
            projections {
                avg('value')
            }
        }
        then:
        average == 2.0
    }

    def "test sum"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def total = Portrait.createCriteria().get {
            projections {
                sum('value')
            }
        }
        then:
        total == 6.0
    }

    def "test Min"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def res = Portrait.createCriteria().get {
            projections {
                min('value')
            }
        }
        then:
        res == 1.0
    }

    def "test Max"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def res = Portrait.createCriteria().get {
            projections {
                max('value')
            }
        }
        then:
        res == 3.0
    }

    def "test equals ignoreCase"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def a = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: artitst, name: 'Soleil Levant', value: 1.0).save()
        def c = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 1.0).save()
        when:
        def results = Portrait.withCriteria {
            eq('name', 'SOLEIL LEVANT', [ignoreCase: true])
        }
        then:
        results == [a, b]
    }

    def "test like"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def a = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: artitst, name: 'Soleil Levant', value: 1.0).save()
        def c = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 1.0).save()
        when:
        def results = Portrait.withCriteria {
            like('name', 'Soleil%')
        }
        then:
        results == [a, b]
    }

    def "test Not"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def a = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: artitst, name: 'Soleil Levant', value: 1.0).save()
        def c = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 1.0).save()
        when:
        def results = Portrait.withCriteria {
            not {
                like('name', 'Soleil%')
            }
        }
        then:
        results == [c]
    }

    def "test Missing Method Exception"(){
        when:

        Portrait.withCriteria {
            myMissingMethod('name', 'Bach')
        }
        fail('where is that method?')


        then:
        MissingMethodException ex = thrown()
        ex.message.contains('.myMissingMethod()')
    }


    def "test Row Count"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def a = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: artitst, name: 'Soleil Levant', value: 1.0).save()
        def c = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 1.0).save()
        when:
        def res = Portrait.withCriteria {
            projections {
                rowCount()
            }
        }
        then:
        res[0] == 3
    }

    // not working in H2
    def "test Row Count And GroupProperty"() {
        given:
        def monet = new Artist(name: 'Monet').save()
        def salvador = new Artist(name: 'Salvador').save()
        def a = new Portrait(artist: monet, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: monet, name: 'Soleil Levant 2', value: 1.0).save()
        def c = new Portrait(artist: salvador, name: 'The Madonna of Port Lligat', value: 1.0).save()
        when:
        def res = Portrait.withCriteria {
            projections {
                rowCount()
                groupProperty('artist')
            }
        }
        then:
        res.find{ it[1].name == 'Monet' }[0] == 2
        res.find{ it[1].name == 'Salvador' }[0] == 1
    }

    def "test Between"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def a = new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        def b = new Portrait(artist: artitst, name: 'Monalisa', value: 10.0).save()
        def c = new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 5.0).save()
        when:
        def res = Portrait.withCriteria {
            between('value', 1.0, 7.0)
        }
        then:
        res.size() == 2
        res[0] == a
        res[1] == c
    }

    def "test EqProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 10.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 10.0, lastSoldPrice: 10.0, name: 'Monalisa', artist: artitst).save()
        def madonna = new Portrait(value: 15.0, lastSoldPrice: 15.0, name: 'The Madonna of Port Lligat', artist: artitst).save()
        when:
        def res = Portrait.withCriteria {
            eqProperty('value', 'lastSoldPrice')
        }
        then:
        res.size() == 2
        res.first() == monalis
        res.last() == madonna
    }

    def "test GeProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 10.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 10.0, lastSoldPrice: 9.0, name: 'Monalisa', artist: artitst).save()
        def madonna = new Portrait(value: 15.0, lastSoldPrice: 19.0, name: 'The Madonna of Port Lligat', artist: artitst).save()
        when:
        def res = Portrait.withCriteria {
            geProperty('lastSoldPrice', 'value')
        }
        then:
        res.size() == 1
        res.first() == madonna
    }

    def "test LeProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 40.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 10.0, lastSoldPrice: 9.0, name: 'Monalisa', artist: artitst).save()
        def madonna = new Portrait(value: 15.0, lastSoldPrice: 50.0, name: 'The Madonna of Port Lligat', artist: artitst).save()
        when:
        def res = Portrait.withCriteria {
            leProperty('lastSoldPrice','value')
        }
        then:
        res.size() == 1
        res.first() == monalis
    }

    def "test NeProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 40.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 10.0, lastSoldPrice: 10.0, name: 'Monalisa', artist: artitst).save()
        when:
        def res = Portrait.withCriteria {
            neProperty('value','lastSoldPrice')
        }
        then:
        res.size() == 1
        res.first() == soleill
    }

    def "test GtProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 40.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 10.0, lastSoldPrice: 19.0, name: 'Monalisa', artist: artitst).save()

        when:
        def res = Portrait.withCriteria {
            gtProperty('lastSoldPrice', 'value')
        }

        then:
        res.size() == 2
        res.first() == soleill
        res.last() == monalis
    }

    def "test LtProperty"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        def soleill = new Portrait(value: 20.0, lastSoldPrice: 40.0, name: 'Soleil levant',artist: artitst ).save()
        def monalis = new Portrait(value: 30.0, lastSoldPrice: 19.0, name: 'Monalisa', artist: artitst).save()
        when:
        def res = Portrait.withCriteria {
            ltProperty('value', 'lastSoldPrice')
        }
        then:
        res.size() == 1
        res.first() == soleill
    }

    def "test Projection Property"(){
        given:
        def monet = new Artist(name: 'Monet').save()

        new Portrait(artist: monet, name: 'Soleil levant 1', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 2', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 3', value: 1.0).save()

        when:
        def rs = Portrait.withCriteria {
            projections {
                property('artist')
            }
        }

        then:
        rs.size() == 3
        rs.each {
            it instanceof Artist
            it.name == 'Monet'
        }
    }

    def "test Projection Properties"(){
        given:
        def monet = new Artist(name: 'Monet').save()

        new Portrait(artist: monet, name: 'Soleil levant 1', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 2', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 3', value: 1.0).save()

        when:
        def rs = Portrait.withCriteria {
            projections {
                property('artist')
                property('value')
            }
        }

        then:
        rs.size() == 3
        rs.each {
            it[0] instanceof Artist
            it[0].name == 'Monet'
            it[1] == 1.0
        }
    }

    def "test projection Property and Sum"(){
        given:
        def monet = new Artist(name: 'Monet').save()

        new Portrait(artist: monet, name: 'Soleil levant 1', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 2', value: 1.0).save()
        new Portrait(artist: monet, name: 'Soleil levant 3', value: 1.0).save()

        when:
        def rs = Portrait.withCriteria {
            projections {
                property('artist')
                sum('value')
            }
        }
        then:
        rs.size() == 1
        rs == [[monet, 3.0]]
    }

    def "test Order By"(){
        given:
        def a = new Artist(name: 'Andreas Achenbach').save()
        def c = new Artist(name: 'Constance Gordon-Cumming').save()
        def b = new Artist(name: 'Botero').save()
        new Portrait(artist: a, name: "Clearing Up—Coast of Sicily").save()
        new Portrait(artist: c, name: "Indian Life at Mirror Lake").save()
        new Portrait(artist: c, name: "Temporary Chimneys and Fire Fountains").save()
        new Portrait(artist: b, name: "Botero's Cat").save()
        when:
        def artistList = Portrait.withCriteria {
            artist {
                order('name', 'asc')
            }
            projections {
                artist {
                    distinct('name')
                }
            }
        }
        then:
        artistList == ['Andreas Achenbach', 'Botero', 'Constance Gordon-Cumming']
    }

    def "test Distinct with Array of Parameters"(){
        given:
        def b = new Artist(name: 'Tomie Oshiro').save()
        new Portrait(artist: b, color: 'Ame', name: 'Cat').save() // Ame == yellow
        new Portrait(artist: b, color: 'Blue', name: 'Fox').save()
        new Portrait(artist: b, color: 'Ame', name: 'Cat').save()
        new Portrait(artist: b, color: 'Blue', name: 'Cat').save()
       when:
        def artistList = Portrait.withCriteria{
            projections {
                distinct(['color', 'name'])
            }
        }
       then:
       artistList.toSet() == [ ['Ame', 'Cat'],
                ['Blue', 'Fox'],
                ['Blue', 'Cat']].toSet()
    }

    def "test sum is null"(){
        given:
        def monet = new Artist(name: 'Monet').save()

        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        when:
        def rs = Portrait.withCriteria {
            projections {
                property('artist')
                sum('value')
            }
        }
        then:
        rs.size() == 1
        rs == [[monet, null]]
    }

    def "test sum with Null values"(){
        given:
        def monet = new Artist(name: 'Monet').save()

        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2', value: 1.1).save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()

        when:
        def rs = Portrait.withCriteria {
            projections {
                property('artist')
                sum('value')
            }
        }
        then:
        rs.size() == 1
        rs == [[monet, 1.1]]
    }

    //version 0.6
    def "test fetch mode"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        when:
        def rs = Portrait.withCriteria {
            eq('artist', monet)
            fetchMode('artist', FetchMode.JOIN)
        }
        then:
        rs.size() == 1
    }

    def "test unique result"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        def portrait = new Portrait(artist: monet, name: 'Soleil levant 1').save()
        when:
        def result = Portrait.withCriteria {
            eq('artist', monet)
            uniqueResult = true
        }
        then:
        result == portrait
    }

    def "test unique result exception"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        when:

        def results = Portrait.withCriteria {
            eq('artist', monet)
            uniqueResult = true
        }
        fail("should throw an exception")
        then:
        org.hibernate.NonUniqueResultException ex = thrown()
        ex.message.contains('query did not return a unique result')
    }

    def "test unique result is null"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        when:
        def res = Portrait.withCriteria {
            eq('artist', monet)
            uniqueResult = true
        }
        then:
        res == null
    }

    def "test plastic criteria on arraList"(){
        given:
        def ls = [
                [name: 'monet', bestPlace: [name: 'Japanese Bridge']],
                [name: 'salvador', bestPlace: [name: 'Catalunya']],
        ]
        when:
        def rs = new PlasticCriteria(ls).list {
            bestPlace {
                eq('name', 'Japanese Bridge')
            }
        }
        then:
        rs.size() == 1
        rs[0].bestPlace.name == 'Japanese Bridge'
    }

    // version 0.7
    def "test list params max"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        new Portrait(artist: monet, name: 'Soleil levant 4').save()
        new Portrait(artist: monet, name: 'Soleil levant 5').save()
        new Portrait(artist: monet, name: 'Soleil levant 6').save()
        new Portrait(artist: monet, name: 'Soleil levant 7').save()
        when:
        def rs = Portrait.createCriteria().list([max: 3]) {
            eq('artist', monet)
        }
        then:
        rs.size() == 3
    }

    def "test list params max and offset"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        new Portrait(artist: monet, name: 'Soleil levant 4').save()
        new Portrait(artist: monet, name: 'Soleil levant 5').save()
        new Portrait(artist: monet, name: 'Soleil levant 6').save()
        new Portrait(artist: monet, name: 'Soleil levant 7').save()
        when:
        def rs = Portrait.createCriteria().list([max: 3, offset: 2]) {
            eq('artist', monet)
        }
        then:
        rs.size() == 3
        rs[0].name == 'Soleil levant 3'
        rs[1].name == 'Soleil levant 4'
        rs[2].name == 'Soleil levant 5'
    }

    def "test list params sort"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        when:
        def rs = Portrait.createCriteria().list([sort: 'name']) {
            eq('artist', monet)
        }
        then:
        rs[0].name == 'Soleil levant 1'
        rs[1].name == 'Soleil levant 2'
        rs[2].name == 'Soleil levant 3'
    }

    def "test list params sort and order"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        when:
        def rs = Portrait.createCriteria().list([sort: 'name', order: 'desc']) {
            eq('artist', monet)
        }
        then:
        rs[0].name == 'Soleil levant 3'
        rs[1].name == 'Soleil levant 2'
        rs[2].name == 'Soleil levant 1'
    }

    //version 0.8
    def "test bug list size smaller than max results"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        new Portrait(artist: monet, name: 'Soleil levant 3').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        when:
        def rs = Portrait.createCriteria().list([sort: 'name', order: 'desc']) {
            maxResults(5)
        }
        then:
        rs.size() == 3
    }

    def "test list size smaller than offset case1"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        when:
        def rs = Portrait.createCriteria().list([max: 3, offset: 5]) {
            eq('artist', monet)
        }
        then:
        rs.size() == 0
        rs.any() == false
    }

    def "test list size smaller than offset case2"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()

        when:
        def rs = Portrait.createCriteria().list([max: 3, offset: 1]) {
            eq('artist', monet)
        }
        then:
        rs.size() == 1
        rs[0].name == 'Soleil levant 2'
    }

    def "test list size smaller than offset case3"(){
        given:
        def monet = new Artist(name: 'Monet').save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()
        new Portrait(artist: monet, name: 'Soleil levant 2').save()
        when:
        def rs = Portrait.createCriteria().list([max: 3, offset: 2]){
            eq('artist', monet)
        }
        then:
        rs.size() == 0
        rs.any() == false
    }

    //version 0.9
    def "test nested objects"(){
        given:
        def paris = new City(name: 'Paris').save()
        def monet = new Artist(name: 'Monet', city: paris).save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        def rio = new City(name: 'Rio de Janeiro').save()
        def portinari = new Artist(name: 'Portinari', city: rio).save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def diCavalcanti = new Artist(name: 'Di Cavalcanti', city: rio).save()
        new Portrait(artist: diCavalcanti, name: 'Autorretrato Com Mulata').save()

        when:
        def rs = Portrait.withCriteria {
            artist {
                city {
                    eq('name', 'Rio de Janeiro')
                }
            }
            order('name', 'asc')
        }

        then:
        rs.size() == 3
        rs.name == ['Autorretrato Com Mulata', 'Paisagem de Brodowski', 'Retirantes']
    }

    //version 1.0
    def "test bug fix Negative array index too large for array size 0"(){
        expect:
        Portrait.createCriteria().get{
            maxResults(1)
        }
        /*then:
        def ex = thrown(Exception)
        ex.message.contains("out of bounds")*/
    }

    //version 1.2
    def "test createAlias"(){
        given:
        def paris = new City(name: 'Paris').save()
        def monet = new Artist(name: 'Monet', city: paris).save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        def rio = new City(name: 'Rio de Janeiro').save()
        def portinari = new Artist(name: 'Portinari', city: rio).save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def diCavalcanti = new Artist(name: 'Di Cavalcanti', city: rio).save()
        new Portrait(artist: diCavalcanti, name: 'Autorretrato Com Mulata').save()

        when:
        def rs = Portrait.withCriteria {
            createAlias('artist', 'genius')
            eq('genius.city', rio)
            order('name', 'asc')
        }
        then:
        rs.size() == 3
        rs.name == ['Autorretrato Com Mulata', 'Paisagem de Brodowski', 'Retirantes']
    }

    //version 1.3
    def "test avg division undefined"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def average = Portrait.createCriteria().get {
            eq('name', 'not exists')
            projections {
                avg('value')
            }
        }
        then:
        average == null
    }

    //version 1.4
    def "test set ReadOnly"(){
        given:
        def artitst = new Artist(name: 'Brilhante').save()
        new Portrait(artist: artitst, name: 'Soleil levant', value: 1.0).save()
        new Portrait(artist: artitst, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: artitst, name: "Les Demoiselles d'Avignon", value: 3.0).save()
        when:
        def list = Portrait.withCriteria {
            setReadOnly(true)
        }
        then:
        list.size() == 3
    }

    //version 1.4.1
    def "test fix null pointer in getProperty"(){
        given:
        def monet = null
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        def rio = new City(name: 'Rio de Janeiro').save()
        def portinari = new Artist(name: 'Portinari', city: rio).save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def diCavalcanti = new Artist(name: 'Di Cavalcanti', city: rio).save()
        new Portrait(artist: diCavalcanti, name: 'Autorretrato Com Mulata').save()

        expect:
        Portrait.count() == 4

        when:
        def rs = Portrait.withCriteria {
            artist {
                city {
                    eq('name', 'Rio de Janeiro')
                }
            }
            order('name', 'asc')
        }
        then:
        rs.size() == 3
        rs.name == ['Autorretrato Com Mulata', 'Paisagem de Brodowski', 'Retirantes']
    }

    //version 1.5
    def "test listDistinct"(){
        given:
        def salvador = new Artist(name: 'Salvador').save()
        new Portrait(artist: salvador, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: salvador, name: "The Persistence of Memory", value: 20.00).save()

        def pablo = new Artist(name: 'Pablo').save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon", value: 10.00).save()

        when:
        def list = Portrait.createCriteria().listDistinct {
            artist {
                eq('name', 'Salvador')
            }
        }

        then:
        list.size() == 2
    }

    //version 1.5.1
    def "test inList"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def salvador = new Artist(name: 'Salvador').save()
        new Portrait(artist: salvador, name: 'The Madonna of Port Lligat', value: 2.0).save()
        new Portrait(artist: salvador, name: "The Persistence of Memory", value: 20.00).save()

        def pablo = new Artist(name: 'Pablo').save()
        new Portrait(artist: pablo, name: "Les Demoiselles d'Avignon", value: 10.00).save()

        when:
        def list = Portrait.withCriteria {
            inList('artist', [pablo, salvador])
        }

        then:
        list.size() == 3
    }

    //version 1.5.2
    def "test CreateAlias Joins"(){
        given:
        def portinari = new Artist(name: "Portinari").save()

        when:
        def leftJoinList = Artist.withCriteria {
            createAlias("portraits", "ps", CriteriaSpecification.LEFT_JOIN)
            eq("name", portinari.name)
        }
        then:
        leftJoinList.size() == 1

        when:
        def innerJoinList = Artist.withCriteria {
            createAlias("portraits", "ps", CriteriaSpecification.INNER_JOIN)
            eq("name", portinari.name)
        }
        then:
        innerJoinList.size() == 0

        // No join specified, defaults to inner join anyway
        when:
        def defaultJoinList = Artist.withCriteria {
            createAlias("portraits", "ps")
            eq("name", portinari.name)
        }
        then:
        defaultJoinList.size() == 0
    }

    def "test Criteria Count"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        when:
        def list = Portrait.withCriteria {
            artist {
                eq("name", portinari.name)
            }
        }
        then:
        list.size() == 2

        when:
        def count = Portrait.createCriteria().count {
            artist {
                eq("name", portinari.name)
            }
        }
        then:
        count == 2
    }

    //version 1.5.3
    def "test Emptiness Restriction"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        def pablo = new Artist(name: 'Pablo').save()
        when:
        def result1 = Artist.withCriteria {
            isEmpty("portraits")
        }
        then:
        result1.size() == 1
        result1[0] == pablo

        when:
        def result2 = Artist.withCriteria {
            isNotEmpty("portraits")
        }

        then:
        result2.size() == 1
        result2[0] == portinari
    }

    //version 1.5.4
    def "test Eq for Collections"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        def pablo = new Artist(name: 'Pablo').save()
        new Portrait(artist: pablo, name: 'Les Demoiselles d’Avignon').save()
        new Portrait(artist: pablo, name: 'Asleep').save()

        when:
        def result1 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            eq 'portraits.name', 'something'
        }
        then:
        result1.size() == 0

        when:
        def result2 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            eq 'portraits.name', 'Retirantes'
        }

        then:
        result2.size() == 1
        result2[0] == portinari

        when:
        def result3 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            eq 'portraits.name', 'asleep', [ignoreCase: true]
        }
        then:
        result3.size() == 1
        result3[0] == pablo
    }

    def "test InList For Instance Value As Collection"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        def portinari2 = new Artist(name: 'Portinari2').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()

        when:
        def result1 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            inList 'portraits.name', 'Retirantes'
        }
        then:
        result1.size() == 1

        when:
        new Portrait(artist: portinari, name: 'Soleil levant').save()
        result1 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            inList 'portraits.name', 'Retirantes'
        }
        then:
        result1.size() == 1

        when:
        new Portrait(artist: portinari2, name: 'Mona').save()
        result1 = Artist.withCriteria {
            createAlias 'portraits', 'portraits'
            inList 'portraits.name', ['Retirantes', 'Mona']
        }
        then:
        assert result1.size() == 2
        assert result1[0] == portinari
        assert result1[1] == portinari2
    }

    //version 1.6
    def "test CountDistinct"(){
        given:
        new Artist(name: 'Portinari').save()
        new Artist(name: 'Portinari').save()
        new Artist(name: 'Pablo').save()
        new Artist(name: 'Pablo').save()
        new Artist(name: 'Salvador').save()

        when:
        def result = Artist.withCriteria {
            projections {
                countDistinct('name')
            }
        }

        then:
        result == [3]
    }

    //version 1.6.1
    def "test SizeEq"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        new Artist(name: 'Salvador').save()
        when:
        def result = Artist.withCriteria {
            sizeEq('portraits', 2)
        }
        then:
        result.size() == 1
        result.first().name == 'Portinari'
    }

    def "test SizeEq zero"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        new Artist(name: 'Salvador').save()
        when:
        def result = Artist.withCriteria {
            sizeEq('portraits', 0)
        }
        then:
        result.size() == 1
        result.first().name == 'Salvador'
    }

    def "test SizeNe"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        new Artist(name: 'Salvador').save()
        when:
        def result = Artist.withCriteria {
            sizeNe('portraits', 2)
        }
        then:
        result.size() == 1
        result.first().name == 'Salvador'
    }

    def "test SizeNe zero"(){
        given:
        def portinari = new Artist(name: 'Portinari').save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()
        new Artist(name: 'Salvador').save()
        when:
        def result = Artist.withCriteria {
            sizeNe('portraits', 0)
        }
        then:
        result.size() == 1
        result.first().name == 'Portinari'
    }

    //version 1.6.2
    def "test Multiple CreateAlias"(){
        given:
        def paris = new City(name: 'Paris').save()
        def monet = new Artist(name: 'Monet', city: paris).save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        def rio = new City(name: 'Rio de Janeiro').save()
        def portinari = new Artist(name: 'Portinari', city: rio).save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def diCavalcanti = new Artist(name: 'Di Cavalcanti', city: rio).save()
        new Portrait(artist: diCavalcanti, name: 'Autorretrato Com Mulata').save()

        when:
        def result = Portrait.withCriteria {
            projections {
                createAlias("artist", "a")
                groupProperty("a.name")
            }
            and {
                createAlias("a.city", "c")
                eq('c.name', 'Rio de Janeiro')
            }
        }

        then:
        result.size() == 2
        result.first() == "Portinari"
        result.last() == 'Di Cavalcanti'
    }

    //version 1.6.3
    def "test CountDistinct On Projection"(){
        given:
        def paris = new City(name: 'Paris').save()
        def monet = new Artist(name: 'Monet', city: paris).save()
        new Portrait(artist: monet, name: 'Soleil levant 1').save()

        def rio = new City(name: 'Rio de Janeiro').save()
        def portinari = new Artist(name: 'Portinari', city: rio).save()
        new Portrait(artist: portinari, name: 'Retirantes').save()
        new Portrait(artist: portinari, name: 'Paisagem de Brodowski').save()

        def diCavalcanti = new Artist(name: 'Di Cavalcanti', city: rio).save()
        new Portrait(artist: diCavalcanti, name: 'Autorretrato Com Mulata').save()

        when:
        def result = Artist.withCriteria {
            projections {
                groupProperty("id")
                createAlias("portraits", "p")
                countDistinct("p.id")
            }
        }

        then:
        result.size() == 3
        result[0] == [monet?.id, 1]
        result[1] == [portinari?.id, 2]
        result[2] == [diCavalcanti?.id, 1]
    }
}
