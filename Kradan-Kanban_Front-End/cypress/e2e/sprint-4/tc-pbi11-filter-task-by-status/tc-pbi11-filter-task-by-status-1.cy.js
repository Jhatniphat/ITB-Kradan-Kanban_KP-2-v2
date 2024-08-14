describe(`TC-PBI10-FILTER-TASK-BY-STATUS-1\n 
          Test Scenario : normal - filter by one status, filter by multiple statuses, filter and sort`, () => {
  
  beforeEach(()=> {
    cy.visit('/task') ;
    cy.wait(100) ;
  }) ;

  it('Open the task view page at /task', () => {
  })

  it('Table shows Title, Assignees and Status', () => {
    cy.contains('Title') ;
    cy.contains('Assignees') ;
    cy.contains('Status') ;
  })

  it('Task table has 6 task entries.',()=>{
    cy.get('.itbkk-item').should('have.length',6) ;
  })

  it('The task list page should have a status sort .itbkk-status-choice and click to sort alphabetical', () => {
    cy.get('.itbkk-status-sort').should('exist').click() ;
  })

  it('The task list page should have a filter by status ', () => {
    cy.get('.itbkk-status-filter').should('exist') ;
  })

  it('Select the "To Do" status in the filter by status ', () => {
    cy.get('.itbkk-status-sort').should('exist').click() ;
    cy.get('.itbkk-status-filter').should('exist').click() ;

    cy.get('.itbkk-status-choice').contains('To Do').click() ;
    cy.wait(100) ;

  })

  it('Task items should be [TD01,TD02]',()=>{
    cy.get('.itbkk-status-sort').should('exist').click() ;
    cy.get('.itbkk-status-filter').should('exist').click() ;

    cy.get('.itbkk-status-choice').contains('To Do').click() ;
    cy.wait(100) ;

    cy.get('.itbkk-item').eq(0).as('item')
    cy.get('@item').contains('.itbkk-title','TD01') ;
    cy.get('@item').contains('.itbkk-status','To Do') ;

    cy.get('.itbkk-item').eq(1).as('item')
    cy.get('@item').contains('.itbkk-title','TD02') ;
    cy.get('@item').contains('.itbkk-status','To Do') ;
  })

  it('Select the "In Progress" status in the filter by status ', () => {
    cy.get('.itbkk-status-sort').should('exist').click() ;
    cy.get('.itbkk-status-filter').should('exist').click() ;

    cy.get('.itbkk-status-choice').contains('To Do').click() ;
    cy.get('.itbkk-status-choice').contains('In Progress').click() ;
    cy.wait(100) ;

  })

  it('Task items should be [IP01, IP02, TD01,TD02]',()=>{
    cy.get('.itbkk-status-sort').should('exist').click() ;
    cy.get('.itbkk-status-filter').should('exist').click() ;

    // if you do not use .itbkk-status-choice, you can change '.itbkk-status-choice' to '.itbkk-status-choice'
    cy.get('.itbkk-status-choice').contains('To Do').click() ;
    cy.get('.itbkk-status-choice').contains('In Progress').click() ;
    cy.wait(100) ;

    cy.get('.itbkk-item').eq(0).as('item')
    cy.get('@item').contains('.itbkk-title','IP01') ;
    cy.get('@item').contains('.itbkk-status','In Progress') ;

    cy.get('.itbkk-item').eq(1).as('item')
    cy.get('@item').contains('.itbkk-title','IP02') ;
    cy.get('@item').contains('.itbkk-status','In Progress') ;

    cy.get('.itbkk-item').eq(2).as('item')
    cy.get('@item').contains('.itbkk-title','TD01') ;
    cy.get('@item').contains('.itbkk-status','To Do') ;

    cy.get('.itbkk-item').eq(3).as('item')
    cy.get('@item').contains('.itbkk-title','TD02') ;
    cy.get('@item').contains('.itbkk-status','To Do') ;
  })

})