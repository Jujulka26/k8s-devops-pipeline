describe('Register Page', () => {
  beforeEach(() => {
    cy.visit('/register')
  })

  it('should display register form', () => {
    cy.get('input[type="text"]').should('be.visible')
    cy.get('input[type="email"]').should('be.visible')
    cy.get('input[type="password"]').should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
  })

  it('should register a new user and redirect to login', () => {
    cy.get('input[type="text"]').type('Test User')
    cy.get('input[type="email"]').type(`test${Date.now()}@gmail.com`)
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/login')
  })

  it('should show error on duplicate email', () => {
    cy.get('input[type="text"]').type('Test User')
    cy.get('input[type="email"]').type('test@gmail.com')
    cy.get('input[type="password"]').type('password123')
    cy.get('button[type="submit"]').click()
    cy.url().should('include', '/register')
  })
})
