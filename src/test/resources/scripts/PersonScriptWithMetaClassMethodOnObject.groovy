class Person {
    String first
    String last
}

Person p = new Person(first: 'Jenn', last: 'Strater')

p.metaClass.getFullName = {
    return delegate.first + ' ' + delegate.last
}

p.fullName
